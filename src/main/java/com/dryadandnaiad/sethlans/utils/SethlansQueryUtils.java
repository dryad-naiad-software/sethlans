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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.ProjectInfo;
import com.dryadandnaiad.sethlans.domains.info.SethlansSettings;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

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

    public static SethlansMode getMode() {
        return SethlansMode.valueOf(SethlansConfigUtils.getProperty(SethlansConfigKeys.MODE, SethlansConfigUtils.getConfigFile()));
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

    public static String getTimeFromMills(Long time) {
        long second = (time / 1000) % 60;
        long minute = (time / (1000 * 60)) % 60;
        long hour = (time / (1000 * 60 * 60));

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
    public static List<String> getChallengeQuestionList() {
        List<String> questionList = new ArrayList<>();
        questionList.add("What is the color you hate the most?");
        questionList.add("What is the name of the movie you hate the most?");
        questionList.add("What is the name of the food you hate the most?");
        questionList.add("What was the make of your first car?");
        questionList.add("In what city or town does your nearest sibling live?");
        questionList.add("What was your childhood nickname?");
        return questionList;
    }

    public static String checkFrameRate(String frameRate) {
        List<String> supportedFrameRates = Arrays.asList("23.98", "24", "25", "29.97", "30", "50", "59.94", "60");
        for (String supportedFrameRate : supportedFrameRates) {
            if (supportedFrameRate.equals(frameRate)) {
                return frameRate;
            }
        }
        return "30";
    }

    public static List<ProjectInfo> convertBlenderProjectsToProjectInfo(List<BlenderProject> projectsToConvert) {
        List<ProjectInfo> projectsToReturn = new ArrayList<>();
        for (BlenderProject blenderProject : projectsToConvert) {
            projectsToReturn.add(convertBlenderProjectToProjectInfo(blenderProject));
        }
        return projectsToReturn;
    }

    public static String getRenderTime(String output, String time) {
        String[] finished = output.split("\\|");
        for (String item : finished) {
            LOG.debug(item);
            if (item.contains("Time:")) {
                time = StringUtils.substringAfter(item, ":");
                time = StringUtils.substringBefore(time, ".");
            }
        }
        return time;
    }

    public static ProjectInfo convertBlenderProjectToProjectInfo(BlenderProject blenderProject) {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setId(blenderProject.getId());
        projectInfo.setReEncode(blenderProject.isReEncode());
        projectInfo.setStartFrame(blenderProject.getStartFrame());
        projectInfo.setEndFrame(blenderProject.getEndFrame());
        projectInfo.setStepFrame(blenderProject.getStepFrame());
        projectInfo.setSamples(blenderProject.getSamples());
        projectInfo.setProjectStatus(blenderProject.getProjectStatus());
        projectInfo.setProjectType(blenderProject.getProjectType());
        projectInfo.setProjectName(blenderProject.getProjectName());
        projectInfo.setSelectedBlenderversion(blenderProject.getBlenderVersion());
        projectInfo.setRenderOn(blenderProject.getRenderOn());
        projectInfo.setTotalRenderTime(getTimeFromMills(blenderProject.getTotalRenderTime()));
        projectInfo.setProjectTime(getTimeFromMills(blenderProject.getTotalProjectTime()));
        projectInfo.setOutputFormat(blenderProject.getRenderOutputFormat());
        projectInfo.setUsername(blenderProject.getSethlansUser().getUsername());
        projectInfo.setFrameRate(blenderProject.getFrameRate());
        projectInfo.setResolutionX(blenderProject.getResolutionX());
        projectInfo.setResolutionY(blenderProject.getResolutionY());
        projectInfo.setBlenderEngine(blenderProject.getBlenderEngine());
        projectInfo.setResPercentage(blenderProject.getResPercentage());
        projectInfo.setPartsPerFrame(blenderProject.getPartsPerFrame());
        projectInfo.setCurrentPercentage(blenderProject.getCurrentPercentage());
        if (projectInfo.getPartsPerFrame() > 1) {
            projectInfo.setUseParts(true);
        } else {
            projectInfo.setUseParts(false);
        }
        projectInfo.setThumbnailPresent(blenderProject.getCurrentFrameThumbnail() != null);
        if (projectInfo.isThumbnailPresent()) {
            projectInfo.setThumbnailURL("/api/project_ui/thumbnail/" + blenderProject.getId() + "/");

        }
        return projectInfo;
    }

    public static boolean isCuda(String deviceID) {
        return deviceID.contains("CUDA");
    }


}
