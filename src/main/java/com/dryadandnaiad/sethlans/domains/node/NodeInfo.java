/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.domains.node;

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 10/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class NodeInfo {
    private String hostname;
    private String ipAddress;
    private String networkPort;
    private BlenderBinaryOS sethlansNodeOS;
    private ComputeType computeType;
    private CPU cpuinfo;
    private String selectedCores;
    private List<GPUDevice> selectedGPUs = new ArrayList<>();
    private List<String> selectedCUDA;

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }


    public String getNetworkPort() {
        return networkPort;
    }

    private static final Logger LOG = LoggerFactory.getLogger(NodeInfo.class);


    public BlenderBinaryOS getSethlansNodeOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                this.sethlansNodeOS = BlenderBinaryOS.Windows64;
            } else {
                this.sethlansNodeOS = BlenderBinaryOS.Windows32;
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            this.sethlansNodeOS = BlenderBinaryOS.MacOS;
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (SystemUtils.OS_ARCH.contains("64")) {
                this.sethlansNodeOS = BlenderBinaryOS.Linux64;
            } else {
                this.sethlansNodeOS = BlenderBinaryOS.Linux32;
            }
        }
        return sethlansNodeOS;
    }


    public ComputeType getComputeType() {
        return computeType;
    }

    public void populateNodeInfo() {
        this.hostname = SethlansUtils.getHostname();

        this.ipAddress = SethlansUtils.getIP();

    }

    public void setNetworkPort(String networkPort) {
        this.networkPort = networkPort;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public List<String> getSelectedCUDA() {
        return selectedCUDA;
    }

    public void setSelectedCUDA(List<String> selectedCUDA) {
        this.selectedCUDA = selectedCUDA;
    }

    public List<GPUDevice> getSelectedGPUs() {
        return selectedGPUs;
    }

    public void setSelectedGPUs() {
        List<GPUDevice> availableGPUs = GPU.listDevices();
        for (String cuda : selectedCUDA) {
            for (GPUDevice gpu : availableGPUs) {
                if (gpu.getCudaName().equals(cuda)){
                    selectedGPUs.add(gpu);
                }
            }
        }
    }

    public CPU getCpuinfo() {
        return cpuinfo;
    }

    public void setCpuinfo() {
        CPU cpu = new CPU();

        this.cpuinfo = cpu;
    }

    public String getSelectedCores() {
        return selectedCores;
    }

    public void setSelectedCores(String selectedCores) {
        this.selectedCores = selectedCores;
    }
}
