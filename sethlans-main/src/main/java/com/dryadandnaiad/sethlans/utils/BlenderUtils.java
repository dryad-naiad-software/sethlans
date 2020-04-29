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

import com.dryadandnaiad.sethlans.models.blender.BlenderFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public static boolean downloadBlender(String blenderVersion, String jsonLocation, String blenderDir) {
        var mapper = new ObjectMapper();
        String blenderDownloadJSON;
        if (jsonLocation.equals("resource")) {
            try {
                blenderDownloadJSON = IOUtils.toString(new SethlansResources("blenderdownload.json").getResource(), StandardCharsets.UTF_8.name());
                log.debug(blenderDownloadJSON);
            } catch (IOException e) {
                log.error("Not a valid resource file");
                log.error(Throwables.getStackTraceAsString(e));
            }
        } else {
            blenderDownloadJSON = QueryUtils.readJSONFromFile(jsonLocation);
        }
        return false;
    }
}
