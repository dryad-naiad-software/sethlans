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
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * File created by Mario Estrella on 4/30/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class PythonUtils {
    static String WINDOWS_ARCHIVE = "python3.7-windows.zip";
    static String LINUX_ARCHIVE = "python3.7-linux.zip";
    static String MACOS_ARCHIVE = "python3.7-macos.zip";

    public static boolean copyPythonArchiveToDisk(String binaryDir, OS os) {
        String filename;
        String path;
        InputStream inputStream;
        try {
            switch (os) {
                case WINDOWS_64:
                    log.info("Downloading Python binary for Windows.");
                    filename = WINDOWS_ARCHIVE;
                    inputStream = new ResourcesUtils("python/" + filename).getResource();
                    path = binaryDir + File.separator + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    log.info("Python binary for Windows downloaded.");
                    return new File(path).exists();
                case LINUX_64:
                    log.info("Downloading Python binary for Linux.");
                    filename = LINUX_ARCHIVE;
                    inputStream = new ResourcesUtils("python/" + filename).getResource();
                    path = binaryDir + File.separator + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    log.info("Python binary for Linux downloaded.");
                    return new File(path).exists();
                case MACOS:
                    log.info("Downloading Python binary for MacOS.");
                    filename = MACOS_ARCHIVE;
                    inputStream = new ResourcesUtils("python/" + filename).getResource();
                    path = binaryDir + File.separator + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    log.info("Python binary for MacOS downloaded.");
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

    public static boolean copyAndExtractScripts(String scriptsDir) {
        String scripts = "scripts.zip";
        String path = scriptsDir + File.separator + scripts;
        try {
            InputStream inputStream = new ResourcesUtils("scripts/scripts.zip").getResource();
            Files.copy(inputStream, Paths.get(path));
            inputStream.close();
            return FileUtils.extractArchive(path, scriptsDir);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static boolean installPython(String binaryDir, OS os) {
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
