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
import com.dryadandnaiad.sethlans.enums.VideoCodec;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.util.FileSystemUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.io.FileUtils.copyFileToDirectory;

/**
 * File created by Mario Estrella on 5/1/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class FFmpegUtils {
    static String WINDOWS_ARCHIVE = "ffmpeg-4.2.2-windows64.tar.xz";
    static String LINUX_ARCHIVE = "ffmpeg-4.2.2-linux64.tar.xz";
    static String MACOS_ARCHIVE = "ffmpeg-4.2.2-macos.tar.xz";
    static String FFMPEG_FILES = "files/ffmpeg/";

    /**
     * @param binaryDir
     * @param os
     * @return
     */
    public static boolean copyFFmpegArchiveToDisk(String binaryDir, OS os) {
        String filename;
        String path;
        InputStream inputStream;
        try {
            switch (os) {
                case WINDOWS_64:
                    log.info("Downloading FFmpeg binary for Windows.");
                    filename = WINDOWS_ARCHIVE;
                    inputStream = new ResourcesUtils(FFMPEG_FILES + filename).getResource();
                    path = binaryDir + File.separator + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    log.info("FFmpeg binary for Windows downloaded.");
                    return new File(path).exists();
                case LINUX_64:
                    log.info("Downloading FFmpeg binary for Linux.");
                    filename = LINUX_ARCHIVE;
                    inputStream = new ResourcesUtils(FFMPEG_FILES + filename).getResource();
                    path = binaryDir + File.separator + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    log.info("FFmpeg binary for Linux downloaded.");
                    return new File(path).exists();
                case MACOS:
                    log.info("Downloading FFmpeg binary for MacOS.");
                    filename = MACOS_ARCHIVE;
                    inputStream = new ResourcesUtils(FFMPEG_FILES + filename).getResource();
                    path = binaryDir + File.separator + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    log.info("FFmpeg binary for MacOS downloaded.");
                    return new File(path).exists();
                default:
                    log.error("Operating System not supported. " + os.getName());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    /**
     * @param binaryDir
     * @param os
     * @return
     */
    public static boolean installFFmpeg(String binaryDir, OS os) {
        switch (os) {
            case WINDOWS_64:
                return FileUtils.extractArchive(binaryDir + File.separator + WINDOWS_ARCHIVE, binaryDir);
            case LINUX_64:
                return FileUtils.extractArchive(binaryDir + File.separator + LINUX_ARCHIVE, binaryDir);
            case MACOS:
                return FileUtils.extractArchive(binaryDir + File.separator + MACOS_ARCHIVE, binaryDir);
            default:
                log.error("Operating System not supported. " + os.getName());
        }
        return false;
    }

    /**
     * @param blenderProject
     * @param ffmpegDir
     * @return
     */
    public static boolean encodeImagesToVideo(Project blenderProject, String ffmpegDir) {
        log.info("Preparing " + blenderProject.getProjectName() + " for video encoding.");
        var videoFile = blenderProject.getProjectSettings().getVideoSettings().getVideoFileLocation();
        String ffmpegBinary = getFFmpegBinary(ffmpegDir);
        if (ffmpegBinary == null) {
            return false;
        }

        if (blenderProject.getFrameFileNames() == null || blenderProject.getFrameFileNames().isEmpty()) {
            log.error("No frame filenames present.");
            return false;
        }
        // Copy images to temporary directory for video processing
        log.info("Copying image files to temporary directory.");
        var tempDir = new File(blenderProject.getProjectRootDir()
                + File.separator + "temp");
        tempDir.mkdirs();
        for (String frameFileName : blenderProject.getFrameFileNames()) {
            try {
                log.debug("Copying " + frameFileName + " to " + tempDir.toString());
                copyFileToDirectory(new File(frameFileName), tempDir);
            } catch (IOException e) {
                log.error("Error copying file: " + frameFileName);
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
                return false;
            }
        }

        var result = executeFFmpegCommand(createFFmpegCommand(ffmpegBinary, blenderProject), blenderProject.getProjectName());
        FileSystemUtils.deleteRecursively(tempDir);
        if (result) {
            return verifyVideoFile(videoFile, getFFprobeBinary(ffmpegDir));
        } else {
            return false;
        }
    }


    private static CommandLine createFFmpegCommand(String ffmpegBinary, Project blenderProject) {
        var videoSettings = blenderProject.getProjectSettings().getVideoSettings();

        var ffmpeg = new CommandLine(ffmpegBinary);
        // Set Framerate
        ffmpeg.addArgument("-framerate");
        ffmpeg.addArgument(videoSettings.getFrameRate().toString());

        // Configure input images
        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(blenderProject.getProjectRootDir() + File.separator + "temp" +
                File.separator +
                QueryUtils.truncatedProjectNameAndID(blenderProject.getProjectName(),
                        blenderProject.getProjectID()) + "-" + "%04d." +
                blenderProject.getProjectSettings().getImageSettings().getImageOutputFormat()
                        .name().toLowerCase());


        // Configure video codec
        ffmpeg.addArgument("-c:v");
        switch (videoSettings.getVideoOutputFormat()) {
            case MP4:
                ffmpeg.addArgument(videoSettings.getCodec().getName());
                ffmpeg.addArgument("-crf");
                ffmpeg.addArgument(videoSettings.getVideoQuality().getName());
                ffmpeg.addArgument("-preset");
                ffmpeg.addArgument("medium");
                ffmpeg.addArgument("-pix_fmt");
                ffmpeg.addArgument(videoSettings.getPixelFormat().getName());
                break;
            case AVI:
                ffmpeg.addArgument(videoSettings.getCodec().getName());
                ffmpeg.addArgument("-pix_fmt");
                ffmpeg.addArgument(videoSettings.getPixelFormat().getName());
                break;
            case MKV:
                ffmpeg.addArgument(videoSettings.getCodec().getName());
                if (videoSettings.getCodec().equals(VideoCodec.LIBX264) || videoSettings.getCodec().equals(VideoCodec.LIBX265)) {
                    ffmpeg.addArgument("-crf");
                    ffmpeg.addArgument(videoSettings.getVideoQuality().getName());
                    ffmpeg.addArgument("-preset");
                    ffmpeg.addArgument("medium");
                }
                ffmpeg.addArgument("-pix_fmt");
                ffmpeg.addArgument(videoSettings.getPixelFormat().getName());
                break;
        }

        // Configure output file
        ffmpeg.addArgument(videoSettings.getVideoFileLocation());
        return ffmpeg;
    }

    private static boolean executeFFmpegCommand(CommandLine command, String projectName) {
        var outputStream = new ByteArrayOutputStream();
        var pumpStreamHandler = new PumpStreamHandler(outputStream);
        var executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.setExitValue(0);
        var resultHandler = new DefaultExecuteResultHandler();
        String output;

        try {
            log.info("Starting video encoding of " + projectName);
            log.debug("Executing the following command " + command.toString());
            executor.execute(command, resultHandler);
            resultHandler.waitFor();
            var out = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
            while ((output = out.readLine()) != null) {
                log.debug(output);
            }
            out.close();

            var exitValue = resultHandler.getExitValue();

            if (exitValue != 0) {
                log.error("Video encoding of " + projectName + " failed!");
                return false;
            }

            log.info("Video encoding of " + projectName + " complete!");
            return true;
        } catch (IOException | InterruptedException e) {
            log.error("Error executing command " + command.toString());
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    private static String getFFmpegBinary(String ffmpegDir) {
        String ffmpegBinary;
        var os = QueryUtils.getOS();
        switch (os) {
            case WINDOWS_64:
                ffmpegBinary = ffmpegDir + File.separator + "bin" + File.separator + "ffmpeg.exe";
                break;
            case LINUX_64:
            case MACOS:
                ffmpegBinary = ffmpegDir + File.separator + "bin" + File.separator + "ffmpeg";
                break;
            default:
                log.error("Operating System not supported. " + os.getName());
                return null;
        }
        return ffmpegBinary;
    }

    private static String getFFprobeBinary(String ffmpegDir) {
        String ffprobeBinary;
        var os = QueryUtils.getOS();
        switch (os) {
            case WINDOWS_64:
                ffprobeBinary = ffmpegDir + File.separator + "bin" + File.separator + "ffprobe.exe";
                break;
            case LINUX_64:
            case MACOS:
                ffprobeBinary = ffmpegDir + File.separator + "bin" + File.separator + "ffprobe";
                break;
            default:
                log.error("Operating System not supported. " + os.getName());
                return null;
        }
        return ffprobeBinary;
    }

    private static boolean verifyVideoFile(String videoFile, String ffprobeBinary) {
        log.info("Verifying that " + videoFile + " was created successfully.");
        String output;
        try {
            output = new ProcessExecutor().command(ffprobeBinary, videoFile)
                    .readOutput(true).exitValues(0).execute().outputUTF8();
            log.debug(output);
            log.info(videoFile + " has been verified.");
            return true;
        } catch (InvalidExitValueException e) {
            log.error("Video file " + videoFile + " is either invalid or does not exist.");
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
