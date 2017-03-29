/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.server;

import com.dryadandnaiad.sethlans.domains.BlenderFile;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataServiceImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created Mario Estrella on 3/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlenderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderUtils.class);
    private static List<BlenderFile> blenderFileList = null;

    private static void getList() {

        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getResult("https://gist.githubusercontent.com/marioestrella/def9d852b3298008ae16040bbbabc524/raw/");
        if (data == null || data.isEmpty()) {
            LOG.debug("Unable to retrieve blenderdownload.json from internet, using local version instead.");
            data = getJSONData.getLocalResult("blenderdownload.json");
        }
        LOG.debug("Retrieved JSON: \n" + data.substring(0, 100) + "...");
        if (data != null || !data.isEmpty()) {
            blenderFileList = new LinkedList<>();

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray downloadArray = jsonData.getJSONArray("blenderdownload");
                for (int i = 0; i < downloadArray.length(); i++) {
                    JSONObject blenderBinary = downloadArray.getJSONObject(i);
                    String version = blenderBinary.getString("version");
                    String macOS = blenderBinary.getString("macos");
                    String windows64 = blenderBinary.getString("windows64");
                    String windows32 = blenderBinary.getString("windows32");
                    String linux64 = blenderBinary.getString("linux64");
                    String linux32 = blenderBinary.getString("linux32");
                    String md5MacOs = blenderBinary.getString("macos_md5");
                    String md5Windows64 = blenderBinary.getString("windows64_md5");
                    String md5Windows32 = blenderBinary.getString("windows32_md5");
                    String md5Linux64 = blenderBinary.getString("linux64_md5");
                    String md5Linux32 = blenderBinary.getString("linux32_md5");
                    BlenderFile blenderFile = new BlenderFile(version, windows32, windows64, macOS, linux32, linux64, md5MacOs, md5Windows64, md5Windows32, md5Linux32, md5Linux64);
                    blenderFileList.add(blenderFile);
                }
            } catch (JSONException jsonEx) {
                LOG.error("Error processing JSON data" + jsonEx.getMessage());
                jsonEx.printStackTrace();
            }
        }
    }

    public static List<BlenderFile> listBinaries() {
        if (blenderFileList == null) {
            getList();
        }
        return blenderFileList;
    }

    public static List<String> listVersions() {
        if (blenderFileList == null) {
            getList();
        }
        List<String> versions = new LinkedList<>();
        for (BlenderFile blenderFile : blenderFileList) {
            versions.add(blenderFile.getBlenderVersion());
        }
        return versions;
    }

    public static void refresh() {
        blenderFileList = null;
    }
}
