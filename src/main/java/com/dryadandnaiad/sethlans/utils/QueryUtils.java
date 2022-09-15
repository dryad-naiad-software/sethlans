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
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.hardware.CPU;
import com.dryadandnaiad.sethlans.models.system.SystemInfo;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.dryadandnaiad.sethlans.utils.ConfigUtils.getProperty;

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
        var si = new oshi.SystemInfo();
        var os = si.getOperatingSystem();
        var bits = os.getBitness();
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
        return OS.UNSUPPORTED;
    }

    /**
     * Returns the hostname for the current system
     *
     * @return String hostname
     */
    public static String getHostname() {
        String hostname = null;
        try {
            hostname = getProperty(ConfigKeys.SETHLANS_HOSTNAME);
            if(hostname == null) {
                hostname = InetAddress.getLocalHost().getHostName();
            }
        } catch (UnknownHostException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
        var indexEnd = Objects.requireNonNull(hostname).indexOf(".");
        if (indexEnd != -1) {
            log.debug(hostname + " contains a domain name. Removing it.");
            hostname = hostname.substring(0, indexEnd);
        }
        return hostname.toUpperCase();
    }

    public static Float versionAsFloat(String version) {
        var index = StringUtils.ordinalIndexOf(version, ".", 2);
        var versionString = version.substring(0,index);
        return Float.parseFloat(versionString);
    }

    public static SystemInfo getCurrentSystemInfo() {
        return SystemInfo.builder()
                .hostname(getHostname())
                .os(getOS())
                .ipAddress(getIP())
                .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                .cpu(new CPU())
                .gpuList(ScanGPU.listDevices())
                .build();
    }

    /**
     * Returns the standard name of a potential file generated from a project.
     * <p>
     * Setting part to null, returns the name of the frame only.
     *
     * @param project
     * @param frame
     * @param part
     * @return
     */
    public static String getFrameAndPartFilename(Project project, Integer frame, Integer part) {
        var projectNameAndID = QueryUtils.truncatedProjectNameAndID(project.getProjectName(),
                project.getProjectID());
        String outputFileName;
        String extension = "";

        switch (project.getProjectSettings().getImageSettings().getImageOutputFormat()) {
            case PNG:
                extension = "png";
                break;
            case TIFF:
                extension = "tif";
                break;
            case HDR:
                extension = "hdr";
        }

        if (part != null) {
            outputFileName = projectNameAndID + "-" + String.format("%04d", frame) + "-" + part + "." +
                    extension;
        } else {
            outputFileName = projectNameAndID + "-" + String.format("%04d", frame) + "." +
                    extension;
        }
        return outputFileName;
    }


    /**
     * Retrieves the available methods. If no GPUs are detected only NodeType.CPU is available.
     *
     * @return Set of NodeType enums
     */
    public static Set<NodeType> getAvailableTypes() {
        var availableMethods = new HashSet<NodeType>();
        if (ScanGPU.listDevices().size() != 0) {
            availableMethods.add(NodeType.CPU_GPU);
            availableMethods.add(NodeType.GPU);
            availableMethods.add(NodeType.CPU);
        } else {
            availableMethods.add(NodeType.CPU);
        }
        return availableMethods;

    }


    public static List<String> challengeQuestions() {
        var questions = new ArrayList<String>();
        questions.add("What city were you born in?");
        questions.add("What is your oldest sibling’s middle name?");
        questions.add("What was the first concert you attended?");
        questions.add("What was the make and model of your first car?");
        questions.add("In what city or town did your parents meet?");
        questions.add("In what city or town was your first job?");
        questions.add("What street did you live on in third grade?");
        return questions;
    }


    /**
     * Retrieves the current version and build.
     *
     * @return String version
     */
    public static String getVersion() {
        val properties = new Properties();
        try {
            properties.load(new InputStreamReader(new ResourceUtils("git.properties").getResource(),
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

    public static String getBuildYear() {
        val properties = new Properties();
        try {
            properties.load(new InputStreamReader(new ResourceUtils("git.properties").getResource(),
                    StandardCharsets.UTF_8));
            String buildYear = properties.getProperty("git.commit.time").substring(0, 4);
            return buildYear;
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

    public static String truncatedProjectNameAndID(String projectName, String uuid) {
        var truncatedProjectName = StringUtils.left(projectName, 10).replaceAll(" ", "")
                .replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
        var truncatedUUID = StringUtils.left(uuid, 4);
        return truncatedProjectName + "-" + truncatedUUID;
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
            return IOUtils.toString(new ResourceUtils(resourceLocation).getResource(),
                    StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            log.error("Resource cannot be read. " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    public static String getIP() {
        String ip = null;
        try {
            ip = getProperty(ConfigKeys.SETHLANS_IP);
            if (ip == null) {
                Socket s = new Socket("8.8.8.8", 53);
                ip = s.getLocalAddress().getHostAddress();
                s.close();
            }
        } catch (IOException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
        return ip;
    }


    public static Long getClientUsedSpace() {
        return getClientTotalSpace() - getClientFreeSpace();
    }


    public static Long getClientFreeSpace() {
        return new File(getProperty(ConfigKeys.TEMP_DIR)).getFreeSpace() / 1024 / 1024 / 1024;
    }

    public static Long getClientTotalSpace() {
        return new File(getProperty(ConfigKeys.TEMP_DIR)).getTotalSpace() / 1024 / 1024 / 1024;

    }
}
