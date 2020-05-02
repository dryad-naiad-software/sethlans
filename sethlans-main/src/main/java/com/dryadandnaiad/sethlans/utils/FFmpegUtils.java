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
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public static boolean encodeImagesToVideo(Project blenderProject, String ffmpegDir) {
        String ffmpegBinary = getFFmpegBinary(ffmpegDir);
        if (ffmpegBinary == null) {
            return false;
        }
        var command = createFFmpegCommand(ffmpegBinary, blenderProject);


        log.debug(ffmpegBinary);


        return false;
    }

    private static CommandLine createFFmpegCommand(String ffmpegBinary, Project blenderProject) {
        var truncatedProjectName = StringUtils.left(blenderProject.getProjectName(), 10);
        var truncatedUUID = StringUtils.left(blenderProject.getProjectID(), 4);
        var cleanedProjectName = truncatedProjectName.replaceAll(" ", "")
                .replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
        var videoSettings = blenderProject.getProjectSettings().getVideoSettings();

        CommandLine ffmpeg = new CommandLine(ffmpegBinary);
        ffmpeg.addArgument("-framerate");
        ffmpeg.addArgument(videoSettings.getFrameRate().toString());
        ffmpeg.addArgument("-i");

        ffmpeg.addArgument(blenderProject.getProjectRootDir() + File.separator + "temp" +
                File.separator + cleanedProjectName + "-" + truncatedUUID + "-" + "%d." +
                blenderProject.getProjectSettings().getImageSettings().getImageOutputFormat()
                        .name().toLowerCase());


        switch (videoSettings.getVideoOutputFormat()) {
            case MP4:
                break;
            case AVI:
                break;
            case MKV:
                break;
        }
        return ffmpeg;
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
}
