/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.utils;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.SethlansSettings;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansQueryUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansQueryUtils.class);
    public static String getShortUUID() {
        return UUID.randomUUID().toString().substring(0, 13);
    }

    public static String getGPUDeviceString(SetupNode setupNode) {
        if (!setupNode.getSelectedGPUDeviceIDs().isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (String deviceID : setupNode.getSelectedGPUDeviceIDs()) {
                if (result.length() != 0) {
                    result.append(",");
                }
                result.append(deviceID);
            }
            return result.toString();
        }
        return null;
    }

    public static SethlansServer getCurrentServerInfo() {
        SethlansServer currentServer = new SethlansServer();
        currentServer.setNetworkPort(getPort());
        currentServer.setHostname(getHostname());
        currentServer.setIpAddress(getIP());
        return currentServer;
    }

    public static SethlansNode getCurrentNodeInfo() {
        SethlansNode currentNode = new SethlansNode();
        currentNode.setNetworkPort(getPort());
        currentNode.setHostname(getHostname());
        currentNode.setIpAddress(getIP());
        return currentNode;
    }

    public static SethlansSettings getSettings() {
        SethlansSettings sethlansSettings = new SethlansSettings();
        String mode = SethlansConfigUtils.getProperty(SethlansConfigKeys.MODE, SethlansConfigUtils.getConfigFile());
        sethlansSettings.setHttpsPort(SethlansQueryUtils.getPort());
        sethlansSettings.setSethlansIP(SethlansQueryUtils.getIP());
        sethlansSettings.setMode(SethlansMode.valueOf(mode));
        sethlansSettings.setRootDir(SethlansConfigUtils.getProperty(SethlansConfigKeys.ROOT_DIR, SethlansConfigUtils.getConfigFile()));
        sethlansSettings.setLogLevel(SethlansConfigUtils.getProperty(SethlansConfigKeys.LOG_LEVEL, SethlansConfigUtils.getConfigFile()));
        return sethlansSettings;
    }

    public static List<ComputeType> getAvailableMethods() {
        List<ComputeType> availableMethods = new ArrayList<>();
        if (GPU.listDevices().size() != 0) {
            availableMethods.add(ComputeType.CPU_GPU);
            availableMethods.add(ComputeType.GPU);
            availableMethods.add(ComputeType.CPU);
        } else {
            availableMethods.add(ComputeType.CPU);
        }
        return availableMethods;

    }

    public static String getHostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        int iend = hostname.indexOf(".");
        if (iend != -1) {
            LOG.debug(hostname + " contains a domain name. Removing it.");
            hostname = hostname.substring(0, iend);
        }
        return hostname.toUpperCase();
    }



    public static String getIP() {
        String ip = null;
        final Properties properties = new Properties();
        try {
            if (SethlansConfigUtils.getConfigFile().exists()) {
                FileInputStream fileIn = new FileInputStream(SethlansConfigUtils.getConfigFile());
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), StandardCharsets.UTF_8));
            }
            ip = properties.getProperty(SethlansConfigKeys.SETHLANS_IP.toString());
            if (ip.equals("null")) {
                if (SystemUtils.IS_OS_LINUX) {
                    // Make a connection to 8.8.8.8 DNS in order to get IP address
                    Socket s = new Socket("8.8.8.8", 53);
                    ip = s.getLocalAddress().getHostAddress();
                    s.close();
                } else {
                    ip = InetAddress.getLocalHost().getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

        return ip;
    }

    public static String getSelectedCores() {
        return SethlansConfigUtils.getProperty(SethlansConfigKeys.CPU_CORES, SethlansConfigUtils.getConfigFile());
    }

    public static boolean getFirstTime() {
        boolean firsttime = true;
        final Properties properties = new Properties();
        try {
            if (SethlansConfigUtils.getConfigFile().exists()) {
                FileInputStream fileIn = new FileInputStream(SethlansConfigUtils.getConfigFile());
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), StandardCharsets.UTF_8));
            }
            firsttime = Boolean.parseBoolean(properties.getProperty(SethlansConfigKeys.FIRST_TIME.toString()));
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return firsttime;
    }

    public static String getPort() {
        String port = null;
        final Properties properties = new Properties();
        try {
            if (SethlansConfigUtils.getConfigFile().exists()) {
                FileInputStream fileIn = new FileInputStream(SethlansConfigUtils.getConfigFile());
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), StandardCharsets.UTF_8));
            }
            port = properties.getProperty(SethlansConfigKeys.HTTPS_PORT.toString());
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return port;
    }

    public static String getOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                return "Windows64";
            } else {
                return "Windows32";
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            return "MacOS";
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (SystemUtils.OS_ARCH.contains("64")) {
                return "Linux64";
            } else {
                return "Linux32";
            }
        }
        return null;
    }

    public static String getVersion() {
        String version = null;

        final Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new Resources("git.properties").getResource(), StandardCharsets.UTF_8));
            String buildNumber = String.format("%04d", Integer.parseInt(properties.getProperty("git.closest.tag.commit.count")));
            version = properties.getProperty("git.build.version") + "." + buildNumber;
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

        if (version == null) {
            // we could not compute the version so use a blank
            version = "";
        }

        return version;
    }

    public static boolean isCuda(String deviceID) {
        return deviceID.contains("CUDA");
    }


}
