/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.devices.ScanGPU;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.models.hardware.CPU;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.models.system.System;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static com.dryadandnaiad.sethlans.utils.PropertiesUtils.getIP;
import static com.dryadandnaiad.sethlans.utils.PropertiesUtils.getPort;

/**
 * File created by Mario Estrella on 4/19/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class QueryUtils {


    /**
     * Creates a short UUID of 13 characters in the format of xxxxxxxx-xxxx
     *
     * @return String output of short UUID
     */
    public static String getShortUUID() {
        return UUID.randomUUID().toString().substring(0, 13);
    }

    /**
     * Returns the operating system name (Windows, Linux, MacOS) as well as if it's 32bits or 64bits
     *
     * @return String in the form of "Windows64"
     */
    public static OS getOS() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        int bits = os.getBitness();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (bits == 64) {
                return OS.WINDOWS_64;
            } else {
                return OS.WINDOWS_32;
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            return OS.MACOS;
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (bits == 64) {
                return OS.LINUX_64;
            } else {
                return OS.LINUX_32;
            }
        }
        return null;
    }

    /**
     * Returns the hostname for the current system
     *
     * @return String hostname
     */
    public static String getHostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
        int indexEnd = hostname.indexOf(".");
        if (indexEnd != -1) {
            log.debug(hostname + " contains a domain name. Removing it.");
            hostname = hostname.substring(0, indexEnd);
        }
        return hostname.toUpperCase();
    }


    /**
     * Retrieves the available methods. If no GPUs are detected only ComputeOn.CPU is available.
     *
     * @return Set of ComputeOn enums
     */
    public static Set<ComputeOn> getAvailableMethods() {
        Set<ComputeOn> availableMethods = new HashSet<>();
        if (ScanGPU.listDevices().size() != 0) {
            availableMethods.add(ComputeOn.CPU_GPU);
            availableMethods.add(ComputeOn.GPU);
            availableMethods.add(ComputeOn.CPU);
            availableMethods.add(ComputeOn.HYBRID);
        } else {
            availableMethods.add(ComputeOn.CPU);
        }
        return availableMethods;

    }

    /**
     * Retrieves base information and returns a System type object
     *
     * @param type Server or Node class.
     * @return Server or Node class populated with basic information
     */
    public static <T extends System> System getCurrentSystemInfo(Class<T> type) {
        if (type.getSimpleName().equals("Server")) {
            return Server.builder()
                    .networkPort(getPort())
                    .hostname(getHostname())
                    .ipAddress(getIP())
                    .build();
        } else {
            return Node.builder()
                    .networkPort(getPort())
                    .hostname(getHostname())
                    .ipAddress(getIP())
                    .os(getOS())
                    .cpu(new CPU())
                    .build();
        }
    }


    /**
     * Retrieves the current version and build.
     *
     * @return String version
     */
    public static String getVersion() {
        final Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new ResourcesUtils("git.properties").getResource(),
                    StandardCharsets.UTF_8));
            String buildNumber = String.format("%04d",
                    Integer.parseInt(properties.getProperty("git.total.commit.count")));
            return properties.getProperty("git.build.version") + "." + buildNumber;
        } catch (IOException e) {
            log.error(Throwables.getStackTraceAsString(e));
            log.error("Unable to access git.properties file");
        }
        return null;
    }

    /**
     * Takes in time in milliseconds(long) and returns it in a readable format.
     *
     * @param time Time in milliseconds
     * @return String hh:mm:ss
     */
    public static String getTimeFromMills(Long time) {
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    /**
     * Parses and returns the string implementation of render time returned from Blender output
     *
     * @param output Blender's output
     * @return String in mm:ss format
     */
    public static String getRenderTime(String output) {
        String time = null;
        String[] finished = output.split("\\|");
        for (String item : finished) {
            if (item.contains("Time:")) {
                time = StringUtils.substringAfter(item, ":");
                time = StringUtils.substringBefore(time, ".");
            }
        }
        return time.replaceAll("\\s", "");
    }

    /**
     * Reads a file and returns it as a String.
     *
     * @param fileLocation
     * @return String format
     */
    public static String getStringFromFile(String fileLocation) {
        FileInputStream input;
        try {
            input = new FileInputStream(fileLocation);
            return IOUtils.toString(input, StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            log.error("File cannot be read. " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * Reads a resource and returns it as a String.
     *
     * @param resourceLocation
     * @return String format
     */
    public static String getStringFromResource(String resourceLocation) {
        try {
            return IOUtils.toString(new ResourcesUtils(resourceLocation).getResource(),
                    StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            log.error("Resource cannot be read. " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }
}
