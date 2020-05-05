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

import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.models.blender.BlendFile;
import com.dryadandnaiad.sethlans.models.blender.BlenderInstallers;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class BlenderUtils {

    public static String executeRenderTask(RenderTask renderTask) {
        return null;
    }

    public static BlendFile parseBlendFile(String blendFile, String scriptsDir, String pythonDir) {
        if (!new File(blendFile).exists()) {
            log.error(blendFile + " does not exist!");
            return null;
        }
        var os = QueryUtils.getOS();
        String pythonBinary;
        var script = scriptsDir + File.separator + "blend_info.py";
        switch (os) {
            case WINDOWS_64:
                pythonBinary = pythonDir + File.separator + "bin" + File.separator + "python.exe";
                break;
            case LINUX_64:
            case MACOS:
                pythonBinary = pythonDir + File.separator + "bin" + File.separator + "python3.7m";
                break;
            default:
                log.error("Operating System not supported. " + os.getName());
                return null;
        }

        String output;
        try {
            output = new ProcessExecutor().command(pythonBinary, script, blendFile)
                    .readOutput(true).exitValues(0).execute().outputUTF8();
        } catch (InvalidExitValueException e) {
            log.error("Process exited with " + e.getExitValue());
            log.error(e.getMessage());
            return null;
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
        var mapper = new ObjectMapper();
        try {
            return mapper.readValue(output, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Unable to process JSON " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    public static boolean extractBlender(String blenderDir, OS os, String fileName, String version) {
        var directoryName = new File(blenderDir +
                File.separator + "blender-" + version + "-" + os.getName().toLowerCase());
        if (os == OS.MACOS) {
            if (FileUtils.extractBlenderFromDMG(fileName, blenderDir)) {
                var directories = FileUtils.listDirectories(blenderDir);
                return new File(blenderDir + File.separator + directories.iterator().next())
                        .renameTo(directoryName);
            }
        } else {
            if (FileUtils.extractArchive(fileName, blenderDir)) {
                var directories = FileUtils.listDirectories(blenderDir);
                return new File(blenderDir + File.separator + directories.iterator().next())
                        .renameTo(directoryName);
            }
        }
        return false;
    }

    public static List<String> availableBlenderVersions(String jsonLocation) {
        var availableVersions = new ArrayList<String>();
        var blenderInstallersList = getInstallersList(jsonLocation);
        for (BlenderInstallers blenderInstallers : blenderInstallersList) {
            availableVersions.add(blenderInstallers.getBlenderVersion());
        }
        return availableVersions;
    }

    public static File downloadBlender(String blenderVersion, String jsonLocation, String blenderDir, OS os) {
        var selectedInstallers = getInstallersByVersion(getInstallersList(jsonLocation), blenderVersion);
        if (selectedInstallers == null) {
            log.error("Blender version: " + blenderVersion + ", or JSON file location: "
                    + jsonLocation + " is incorrect");
            return null;
        }
        List<String> downloadURLs;
        String md5;

        switch (os) {
            case MACOS:
                downloadURLs = selectedInstallers.getMacOS();
                md5 = selectedInstallers.getMd5MacOS();
                break;
            case WINDOWS_64:
                downloadURLs = selectedInstallers.getWindows64();
                md5 = selectedInstallers.getMd5Windows64();
                break;
            case LINUX_64:
                downloadURLs = selectedInstallers.getLinux64();
                md5 = selectedInstallers.getMd5Linux64();
                break;
            default:
                log.error("Invalid OS given. " + os.getName() + " is not supported.");
                return null;
        }
        var fileToSave = new File(blenderDir + File.separator +
                blenderVersion + "-" + os.getName().toLowerCase() +
                FileUtils.getExtensionFromString(downloadURLs.get(0)));
        for (String downloadURL : downloadURLs) {
            try {
                DownloadFile.downloadFileWithResume(downloadURL, fileToSave.toString());
                if (FileUtils.fileCheckMD5(fileToSave, md5)) {
                    return fileToSave;
                } else {
                    return null;
                }
            } catch (FileNotFoundException e) {
                log.error("Not found, either link is down or mirror is gone. " + e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            } catch (IOException | URISyntaxException e) {
                log.error(Throwables.getStackTraceAsString(e));
                return null;
            }
        }
        return null;
    }

    private static List<BlenderInstallers> getInstallersList(String jsonLocation) {
        String blenderDownloadJSON;
        List<BlenderInstallers> blenderInstallersList;


        if (jsonLocation.equals("resource")) {
            blenderDownloadJSON = QueryUtils.getStringFromResource("blenderdownload.json");

        } else {
            blenderDownloadJSON = QueryUtils.getStringFromFile(jsonLocation);
        }
        if (blenderDownloadJSON == null) {
            return null;
        }
        var mapper = new ObjectMapper();


        try {
            blenderInstallersList = mapper.readValue(blenderDownloadJSON, new TypeReference<>() {
            });
            return blenderInstallersList;
        } catch (JsonProcessingException e) {
            log.error("Unable to process JSON " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }

    }

    private static BlenderInstallers getInstallersByVersion(List<BlenderInstallers> blenderInstallersList,
                                                            String blenderVersion) {

        for (BlenderInstallers blenderInstallers : blenderInstallersList) {
            if (blenderInstallers.getBlenderVersion().equals(blenderVersion)) {
                return blenderInstallers;
            }
        }
        return null;
    }


}
