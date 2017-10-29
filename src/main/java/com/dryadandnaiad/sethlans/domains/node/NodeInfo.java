package com.dryadandnaiad.sethlans.domains.node;

import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import org.apache.commons.lang3.SystemUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
    private BlenderBinaryOS nodeOS;
    private ComputeType computeType;

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }


    public String getNetworkPort() {
        return networkPort;
    }


    public BlenderBinaryOS getNodeOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                this.nodeOS = BlenderBinaryOS.Windows64;
            } else {
                this.nodeOS = BlenderBinaryOS.Windows32;
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            this.nodeOS = BlenderBinaryOS.MacOS;
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (SystemUtils.OS_ARCH.contains("64")) {
                this.nodeOS= BlenderBinaryOS.Linux64;
            } else {
                this.nodeOS = BlenderBinaryOS.Linux32;
            }
        }
        return nodeOS;
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
}
