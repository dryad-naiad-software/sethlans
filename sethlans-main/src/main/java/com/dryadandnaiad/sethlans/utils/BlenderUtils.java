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
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class BlenderUtils {

    public static BlenderFile parseBlenderFile(String blenderFile, String scriptsDir, String pythonBinary) {
        return null;
    }

    public static boolean downloadBlender(String blenderVersion, String jsonLocation, String blenderDir, OS os) {
        BlenderInstallers selectedInstallers = getInstallers(jsonLocation, blenderVersion);
        if (selectedInstallers == null) {
            log.error("Blender version: " + blenderVersion + ", or JSON file location: " + jsonLocation + " is incorrect");
            return false;
        }
        List<String> downloadURLs = null;

        switch (os) {
            case MACOS:
                downloadURLs = selectedInstallers.getMacOS();
                break;
            case WINDOWS_64:
                downloadURLs = selectedInstallers.getWindows64();
                break;
            case LINUX_64:
                downloadURLs = selectedInstallers.getLinux64();
                break;
            default:
                log.error("Invalid OS given. " + os.getName() + " is not supported.");
                return false;
        }
        File fileToSave = new File(blenderDir + File.separator +
                blenderVersion + "-" + os.getName().toLowerCase() +
                FileUtils.getExtensionFromString(downloadURLs.get(0)));
        for (String downloadURL : downloadURLs) {
            try {
                long size = DownloadFile.downloadFileWithResume(downloadURL, fileToSave.toString());
                if (size > 0) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                log.error("Not found, either link is down or mirror is gone. " + e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            } catch (IOException | URISyntaxException e) {
                log.error(Throwables.getStackTraceAsString(e));
                return false;
            }
        }
        return false;
    }

    private static BlenderInstallers getInstallers(String jsonLocation, String blenderVersion) {
        var mapper = new ObjectMapper();
        String blenderDownloadJSON;
        List<BlenderInstallers> blenderInstallersList;

        if (jsonLocation.equals("resource")) {
            try {
                blenderDownloadJSON = IOUtils.toString(new ResourcesUtils("blenderdownload.json").getResource(),
                        StandardCharsets.UTF_8.name());
            } catch (JsonProcessingException e) {
                log.error("Unable to process JSON " + e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
                return null;
            } catch (IOException e) {
                log.error("Not a valid resource file");
                log.error(Throwables.getStackTraceAsString(e));
                return null;
            }

        } else {
            blenderDownloadJSON = QueryUtils.readStringFromFile(jsonLocation);
        }

        try {
            blenderInstallersList = mapper.readValue(blenderDownloadJSON, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Unable to process JSON " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
        for (BlenderInstallers blenderInstallers : blenderInstallersList) {
            if (blenderInstallers.getBlenderVersion().equals(blenderVersion)) {
                return blenderInstallers;
            }
        }
        return null;
    }
}
