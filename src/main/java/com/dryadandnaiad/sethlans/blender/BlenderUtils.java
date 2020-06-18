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

package com.dryadandnaiad.sethlans.blender;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.models.blender.BlendFile;
import com.dryadandnaiad.sethlans.models.blender.BlenderInstallers;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.utils.DownloadFile;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.util.FileSystemUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

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

    public static String getBlenderExecutable(String binaryDir, String version) {
        var os = QueryUtils.getOS();
        var directory = new File(binaryDir + File.separator + "blender-" + version + "-" +
                os.getName().toLowerCase());
        if (!directory.exists()) {
            return null;
        }
        File file;
        switch (os) {
            case WINDOWS_64:
                file = new File(directory + File.separator + "blender.exe");
                if (file.exists()) {
                    return file.toString();
                }
                return null;

            case LINUX_64:
                file = new File(directory + File.separator + "blender");
                if (!file.exists()) return null;
                if (makeExecutable(file.toString())) {
                    return file.toString();
                }
                return null;
            case MACOS:
                if (version.contains("2.79b")) {
                    file = new File(directory + File.separator +
                            "MacOS" + File.separator + "blender");
                } else {
                    file = new File(directory + File.separator +
                            "MacOS" + File.separator + "Blender");
                }

                if (!file.exists()) return null;
                if (makeExecutable(file.toString())) {
                    return file.toString();
                }
                return null;
            default:
                log.error("Operating System not supported. " + os.getName());
                return null;
        }

    }

    private static boolean makeExecutable(String file) {
        try {
            new ProcessExecutor().command("chmod", "+x", file)
                    .readOutput(true).exitValues(0).execute();
            return true;
        } catch (InvalidExitValueException e) {
            log.error("Process exited with " + e.getExitValue());
            log.error(e.getMessage());
            return false;
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }


    public static Long executeRenderTask(RenderTask renderTask, boolean debug) {
        var projectNameAndID = QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(),
                renderTask.getProjectID());
        String outputPathAndFilename;
        if (renderTask.isUseParts()) {
            outputPathAndFilename = renderTask.getTaskDir()
                    + File.separator + projectNameAndID + "-"
                    + "####" + "-"
                    + renderTask.getFrameInfo().getPartNumber();
        } else {
            outputPathAndFilename = renderTask.getTaskDir()
                    + File.separator + projectNameAndID + "-" + "####";
        }

        log.info("Starting to render of `" + renderTask.getProjectName() + "`");
        log.info("Frame: " + renderTask.getFrameInfo().getFrameNumber());
        if (renderTask.isUseParts()) {
            log.info("Part: " + renderTask.getFrameInfo().getPartNumber());
        }
        String output;

        try {
            var startTime = System.currentTimeMillis();
            if (debug) {
                output = new ProcessExecutor().command(renderTask.getBlenderExecutable(), "-d", "-b",
                        renderTask.getTaskBlendFile(), "-P", renderTask.getTaskDir() + File.separator +
                                renderTask.getTaskID() + ".py", "-o", outputPathAndFilename, "-f",
                        renderTask.getFrameInfo().getFrameNumber().toString())
                        .readOutput(true).exitValues(0).execute().outputUTF8();
            } else {
                output = new ProcessExecutor().command(renderTask.getBlenderExecutable(), "-b",
                        renderTask.getTaskBlendFile(), "-P", renderTask.getTaskDir() + File.separator +
                                renderTask.getTaskID() + ".py", "-o", outputPathAndFilename, "-f",
                        renderTask.getFrameInfo().getFrameNumber().toString())
                        .readOutput(true).exitValues(0).execute().outputUTF8();
            }
            var endTime = System.currentTimeMillis();
            log.debug(output);
            return endTime - startTime;


        } catch (InvalidExitValueException e) {
            log.error("Process exited with " + e.getExitValue());
            if (renderTask.getScriptInfo().getBlenderEngine().equals(BlenderEngine.BLENDER_EEVEE) &&
                    QueryUtils.getOS().equals(OS.LINUX_64)) {
                log.error("Eevee failed to render, check that display is available if running Linux!");
            }
            log.error(e.getMessage());
            return null;
        } catch (InterruptedException | IOException | TimeoutException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
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

    public static boolean extractBlender(String binaryDir, OS os, String fileName, String version) {
        var tempDirectory = new File(binaryDir + File.separator + "temp" + File.separator);
        var directoryName = new File(binaryDir +
                File.separator + "blender-" + version + "-" + os.getName().toLowerCase());
        if (os == OS.MACOS) {
            if (extractBlenderFromDMG(fileName, tempDirectory.toString())) {
                var directories = FileUtils.listDirectories(tempDirectory.toString());
                if (new File(tempDirectory + File.separator + directories.iterator().next())
                        .renameTo(directoryName)) {
                    log.info("Blender " + version + " installed in " + directoryName);
                    return FileSystemUtils.deleteRecursively(tempDirectory);
                }
            }
        } else {
            if (FileUtils.extractArchive(fileName, tempDirectory.toString())) {
                var directories = FileUtils.listDirectories(tempDirectory.toString());
                if (new File(tempDirectory + File.separator + directories.iterator().next())
                        .renameTo(directoryName)) {
                    log.info("Blender " + version + " installed in " + directoryName);
                    return FileSystemUtils.deleteRecursively(tempDirectory);
                }
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



    public static File downloadBlenderToServer(String blenderVersion, String jsonLocation, String downloadDir, OS os) {
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
        var fileToSave = new File(downloadDir + File.separator +
                blenderVersion + "-" + os.getName().toLowerCase() +
                FileUtils.getExtensionFromString(downloadURLs.get(0)));
        for (String downloadURL : downloadURLs) {
            try {
                DownloadFile.downloadFileWithResume(downloadURL, fileToSave.toString());
                if (FileUtils.fileCheckMD5(fileToSave, md5)) {
                    return fileToSave;
                } else {
                    fileToSave.delete();
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


    /**
     * @param dmgFile
     * @return
     */
    public static boolean extractBlenderFromDMG(String dmgFile, String destination) {
        if (SystemUtils.IS_OS_MAC) {
            log.info("Copying contents of " + dmgFile + " to " + destination);
            try {
                int exit = new ProcessExecutor().command("hdiutil", "mount", dmgFile)
                        .redirectOutput(Slf4jStream.ofCaller().asDebug()).execute().getExitValue();
                if (exit > 0) {
                    return false;
                }
                if (dmgFile.contains("2.79b")) {
                    exit = new ProcessExecutor().command("cp", "-R", "/Volumes/Blender/Blender/blender.app", destination)
                            .redirectOutput(Slf4jStream.ofCaller().asDebug()).execute().getExitValue();
                } else {
                    exit = new ProcessExecutor().command("cp", "-R", "/Volumes/Blender/Blender.app", destination)
                            .redirectOutput(Slf4jStream.ofCaller().asDebug()).execute().getExitValue();
                }

                if (exit > 0) {
                    return false;
                }
                exit = new ProcessExecutor().command("hdiutil", "unmount", "/Volumes/Blender/")
                        .redirectOutput(Slf4jStream.ofCaller().asDebug()).execute().getExitValue();
                new File(dmgFile).delete();
                return exit <= 0;
            } catch (IOException | InterruptedException | TimeoutException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            }
        }
        return false;
    }
}
