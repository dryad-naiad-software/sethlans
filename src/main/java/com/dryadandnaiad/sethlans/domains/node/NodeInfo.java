package com.dryadandnaiad.sethlans.domains.node;

import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import org.apache.commons.lang3.SystemUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private String cores;
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

    public void populateNodeInfo() throws UnknownHostException {
        this.hostname = InetAddress.getLocalHost().getHostName();
        this.ipAddress = InetAddress.getLocalHost().getHostAddress();

    }

    public void setNetworkPort(String networkPort) {
        this.networkPort = networkPort;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public void setCores(String cores) {
        this.cores = cores;
    }

    public String getCores() {
        return cores;
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
}
