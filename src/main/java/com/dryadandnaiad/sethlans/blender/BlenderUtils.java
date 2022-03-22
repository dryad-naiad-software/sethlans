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

import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.models.blender.BlendFile;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.BlenderExecutable;
import com.dryadandnaiad.sethlans.models.blender.BlenderInstallers;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.util.FileSystemUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class BlenderUtils {

    public static boolean copyBenchmarkToDisk(String benchmarkDir) {
        try {
            var inputStream = new ResourceUtils("files/benchmark/bmw27.blend").getResource();
            var path = benchmarkDir + File.separator + "bmw27.blend";
            Files.copy(inputStream, Paths.get(path));
            inputStream.close();
            return new File(path).exists();
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }

    }

    public static BlenderExecutable getLatestExecutable() {
        var installedExecutables = PropertiesUtils.getInstalledBlenderExecutables();
        var versions = new ArrayList<String>();
        for (BlenderExecutable installedExecutable : installedExecutables) {
            versions.add(installedExecutable.getBlenderVersion());
        }
        versions.sort(new AlphaNumericComparator());
        for (BlenderExecutable installedExecutable : installedExecutables) {
            if (installedExecutable.getBlenderVersion().equals(versions.get(0))) {
                return installedExecutable;
            }
        }
        return installedExecutables.get(0);
    }

    public static boolean requestBlenderFromServer(String version, Server server) {
        var path = "/api/v1/server_queue/get_blender_executable";
        var host = server.getIpAddress();
        var port = server.getNetworkPort();
        var params = ImmutableMap.<String, String>builder()
                .put("system-id", PropertiesUtils.getSystemID())
                .put("os", QueryUtils.getOS().toString())
                .put("version", version)
                .build();

        var json = NetworkUtils.getJSONWithParams(path, host, port, params, true);
        if (json == null || json.isEmpty()) {
            log.info("Requested version is not on the server");
            return false;
        } else {
            var objectMapper = new ObjectMapper();
            try {
                var blenderArchive = objectMapper
                        .readValue(json, BlenderArchive.class);
                return installBlenderFromServer(blenderArchive, server, PropertiesUtils.getSystemID());

            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
                return false;
            }
        }

    }

    public static boolean downloadImageFileFromNode(RenderTask renderTask, Node node, File directory) {
        try {
            var imageFileMd5 = renderTask.getTaskImageFileMD5Sum();
            var imageFileName = renderTask.getTaskImageFile();
            String tempPath = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR)
                    + File.separator + QueryUtils.getShortUUID() + imageFileName;
            String fullPath = directory + File.separator + imageFileName;


            var serverSystemID = PropertiesUtils.getSystemID();


            var downloadURL = new URL("https://" + node.getIpAddress() + ":" + node.getNetworkPort() +
                    "/api/v1/node_task/retrieve_image_file?system-id=" + serverSystemID + "&task-id=" +
                    renderTask.getTaskID());
            var downloadedFile = DownloadFile.downloadFileBetweenSethlans(downloadURL,
                    tempPath);
            if (downloadedFile == null) {
                return false;
            }

            if (FileUtils.fileCheckMD5(downloadedFile, imageFileMd5)) {
                var tempFile = new File(tempPath);
                org.apache.commons.io.FileUtils.copyFile(tempFile, new File(fullPath));
                tempFile.delete();
                return true;
            } else {
                return false;

            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }


    public static String downloadProjectFileFromServer(RenderTask renderTask, Server server) {
        try {
            String tempPath;
            String zipFileName;
            String zipFileNameMd5 = null;

            var projectFileMd5 = renderTask.getTaskBlendFileMD5Sum();
            var projectFileName = renderTask.getTaskBlendFile();
            var projectFilePath = new File(ConfigUtils.getProperty(ConfigKeys.BLEND_FILE_CACHE_DIR) + File.separator + renderTask.getProjectID());
            var fullPath = projectFilePath + File.separator + projectFileName;
            projectFilePath.mkdirs();
            var nodeSystemID = PropertiesUtils.getSystemID();


            if (renderTask.isZipFileProject()) {
                zipFileName = renderTask.getZipFile();
                zipFileNameMd5 = renderTask.getZipFileMD5Sum();
                tempPath = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR) + File.separator + QueryUtils.getShortUUID() + zipFileName;
            } else {
                tempPath = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR) + File.separator + QueryUtils.getShortUUID() + projectFileName;
            }

            var downloadURL = new URL("https://" + server.getIpAddress() + ":" + server.getNetworkPort() +
                    "/api/v1/server_queue/retrieve_project_file?system-id=" + nodeSystemID + "&project-id=" +
                    renderTask.getProjectID());
            var downloadedFile = DownloadFile.downloadFileBetweenSethlans(downloadURL,
                    tempPath);
            if (downloadedFile == null) {
                return null;
            }

            if (renderTask.isZipFileProject()) {
                if (FileUtils.fileCheckMD5(downloadedFile, zipFileNameMd5)) {
                    org.apache.commons.io.FileUtils.copyFile(new File(tempPath), new File(fullPath));
                    FileUtils.extractArchive(downloadedFile.toString(), projectFilePath.toString(), true);
                    return fullPath;
                } else {
                    return null;
                }
            } else {
                if (FileUtils.fileCheckMD5(downloadedFile, projectFileMd5)) {
                    var tempFile = new File(tempPath);
                    org.apache.commons.io.FileUtils.copyFile(tempFile, new File(fullPath));
                    tempFile.delete();
                    return downloadedFile.toString();
                } else {
                    return null;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }


    public static boolean installBlenderFromServer(BlenderArchive blenderArchive,
                                                   Server server, String nodeSystemID) {
        try {
            var archiveMd5 = blenderArchive.getBlenderFileMd5();

            var blenderArchiveFilename = Paths.get(blenderArchive.getBlenderFile()).getFileName().toString();
            var downloadFullPath = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR) +
                    File.separator + blenderArchiveFilename;
            var downloadURL = new URL("https://" + server.getIpAddress() + ":" + server.getNetworkPort() +
                    "/api/v1/server_queue/get_blender_executable?system-id=" + nodeSystemID + "&archive-os=" +
                    blenderArchive.getBlenderOS() + "&archive-version=" + blenderArchive.getBlenderVersion());
            var downloadedFile = DownloadFile.downloadFileBetweenSethlans(downloadURL,
                    downloadFullPath);
            if (downloadedFile == null) {
                return false;
            }
            var binaryDir = ConfigUtils.getProperty(ConfigKeys.BINARY_DIR);
            if (FileUtils.fileCheckMD5(downloadedFile, archiveMd5)) {
                if (extractBlender(binaryDir, blenderArchive.getBlenderOS(), downloadedFile.toString(),
                        blenderArchive.getBlenderVersion())) {
                    var blenderExecutable =
                            BlenderExecutable.builder()
                                    .os(blenderArchive.getBlenderOS())
                                    .blenderExecutable(getBlenderExecutable(binaryDir, blenderArchive.getBlenderVersion()))
                                    .blenderVersion(blenderArchive.getBlenderVersion()).build();
                    var blenderExecutableList = PropertiesUtils.getInstalledBlenderExecutables();
                    blenderExecutableList.add(blenderExecutable);
                    PropertiesUtils.updateInstalledBlenderExecutables(blenderExecutableList);
                    return true;
                }
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
        return false;
    }


    public static String getBlenderExecutable(String binaryDir, String version) {
        var os = QueryUtils.getOS();
        var directory = new File(binaryDir + File.separator + "blender-" + version + "-" +
                os.getName().toLowerCase());
        if (!directory.exists()) {
            return null;
        }
        File file;
        switch (os) {
            case WINDOWS_64 -> {
                file = new File(directory + File.separator + "blender.exe");
                if (file.exists()) {
                    return file.toString();
                }
                return null;
            }
            case LINUX_64 -> {
                file = new File(directory + File.separator + "blender");
                if (!file.exists()) return null;
                if (makeExecutable(file.toString())) {
                    return file.toString();
                }
                return null;
            }
            case MACOS -> {
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
            }
            default -> {
                log.error("Operating System not supported. " + os.getName());
                return null;
            }
        }

    }

    public static void setImageFileName(RenderTask renderTask) {
        var frame = renderTask.getFrameInfo().getFrameNumber();
        var part = renderTask.getFrameInfo().getPartNumber();
        var projectID = renderTask.getProjectID();
        var extension = renderTask.getScriptInfo().getImageOutputFormat();
        if (renderTask.isUseParts()) {
            renderTask.setTaskImageFile(projectID + "-frame-" + frame + "-part-"
                    + part + "." + extension.name().toLowerCase());
        } else {
            renderTask.setTaskImageFile(projectID + "-frame-" + frame + "." + extension.name().toLowerCase());
        }
    }


    public static Long executeRenderTask(RenderTask renderTask, boolean debug) {

        String outputPath = renderTask.getTaskDir() + File.separator + "result";

        log.info("Starting to render of `" + renderTask.getProjectName() + "`");
        log.info("Frame: " + renderTask.getFrameInfo().getFrameNumber());
        if (renderTask.isUseParts()) {
            log.info("Part: " + renderTask.getFrameInfo().getPartNumber());
        }
        String output;

        try {
            var startTime = new Date().getTime();
            if (debug) {
                output = new ProcessExecutor().command(renderTask.getBlenderExecutable(), "-d", "-b",
                                renderTask.getTaskBlendFile(), "-P", renderTask.getTaskDir() + File.separator +
                                        renderTask.getTaskID() + ".py", "-o", outputPath, "-f",
                                renderTask.getFrameInfo().getFrameNumber().toString()).destroyOnExit()
                        .readOutput(true).exitValues(0).execute().outputUTF8();
            } else {
                output = new ProcessExecutor().command(renderTask.getBlenderExecutable(), "-b",
                                renderTask.getTaskBlendFile(), "-P", renderTask.getTaskDir() + File.separator +
                                        renderTask.getTaskID() + ".py", "-o", outputPath, "-f",
                                renderTask.getFrameInfo().getFrameNumber().toString()).destroyOnExit()
                        .readOutput(true).exitValues(0).execute().outputUTF8();
            }
            var endTime = new Date().getTime();
            log.debug(output);
            var taskDir = new File(renderTask.getTaskDir());
            File[] files = taskDir.listFiles((dir1, name) -> name.startsWith("result"));
            if (files != null && files.length == 1) {
                files[0].renameTo(new File(renderTask.getTaskDir() + File.separator
                        + renderTask.getTaskImageFile()));
                log.debug("Renaming file after render.");
            }
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
            case WINDOWS_64 -> pythonBinary = pythonDir + File.separator + "bin" + File.separator + "python.exe";
            case LINUX_64, MACOS -> pythonBinary = pythonDir + File.separator + "bin" + File.separator + "python3.7m";
            default -> {
                log.error("Operating System not supported. " + os.getName());
                return null;
            }
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
            if (FileUtils.extractArchive(fileName, tempDirectory.toString(), true)) {
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

    public static List<String> availableBlenderVersions() {
        var availableVersions = new ArrayList<String>();
        var blenderInstallersList = getInstallersList();
        for (BlenderInstallers blenderInstallers : blenderInstallersList) {
            availableVersions.add(blenderInstallers.getBlenderVersion());
        }
        return availableVersions;
    }


    public static File downloadBlenderToServer(String blenderVersion, String downloadDir, OS os) {
        var selectedInstallers = getInstallersByVersion(Objects.requireNonNull(getInstallersList()),
                blenderVersion);
        if (selectedInstallers == null) {
            log.error("Blender version: " + blenderVersion + " is incorrect");
            return null;
        }
        List<String> downloadURLs;
        String md5;

        switch (os) {
            case MACOS -> {
                downloadURLs = selectedInstallers.getMacOS();
                md5 = selectedInstallers.getMd5MacOS();
            }
            case WINDOWS_64 -> {
                downloadURLs = selectedInstallers.getWindows64();
                md5 = selectedInstallers.getMd5Windows64();
            }
            case LINUX_64 -> {
                downloadURLs = selectedInstallers.getLinux64();
                md5 = selectedInstallers.getMd5Linux64();
            }
            default -> {
                log.error("Invalid OS given. " + os.getName() + " is not supported.");
                return null;
            }
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

    private static List<BlenderInstallers> getInstallersList() {
        String blenderDownloadJSON =
                "https://raw.githubusercontent.com/dryad-naiad-software/sethlans/1.5.0_spring5/blenderdownloads.json";
        List<BlenderInstallers> blenderInstallersList;
        var mapper = new ObjectMapper();


        try {
            blenderInstallersList = mapper.readValue(new URL(blenderDownloadJSON), new TypeReference<>() {
            });
            return blenderInstallersList;
        } catch (IOException e) {
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

    private static boolean extractBlenderFromDMG(String dmgFile, String destination) {
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
}
