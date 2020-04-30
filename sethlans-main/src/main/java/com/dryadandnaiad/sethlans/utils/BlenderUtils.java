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
import com.dryadandnaiad.sethlans.models.blender.BlenderFile;
import com.dryadandnaiad.sethlans.models.blender.BlenderInstallers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class BlenderUtils {

    public static BlenderFile parseBlenderFile(String blenderFile, String scriptsDir, String blenderDir) {
        return null;
    }

    public static boolean extractBlender(String blenderDir, OS os, String fileName) {
        if (os == OS.MACOS) {
            FileUtils.extractBlenderFromDMG(fileName, blenderDir);
        } else {
            FileUtils.extractArchive(fileName, blenderDir);
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
        BlenderInstallers selectedInstallers = getInstallersByVersion(getInstallersList(jsonLocation), blenderVersion);
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