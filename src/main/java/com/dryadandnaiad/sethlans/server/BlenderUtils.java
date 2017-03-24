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

import com.dryadandnaiad.sethlans.domains.BlenderObject;
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
    private static List<BlenderObject> blenderObjectList = null;

    private static void getList() {

        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getResult("https://gist.githubusercontent.com/marioestrella/def9d852b3298008ae16040bbbabc524/raw/");
        if (data == null || data.isEmpty()) {
            LOG.debug("Unable to retrieve blenderdownload.json from internet, using local version instead.");
            data = getJSONData.getLocalResult("blenderdownload.json");
        }
        LOG.debug("Retrieved JSON: \n" + data.substring(0, 100) + "...");
        if (data != null || !data.isEmpty()) {
            blenderObjectList = new LinkedList<>();

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
                    BlenderObject blenderObject = new BlenderObject(version, windows32, windows64, macOS, linux32, linux64);
                    blenderObjectList.add(blenderObject);
                }
            } catch (JSONException jsonEx) {
                LOG.error("Error processing JSON data" + jsonEx.getMessage());
                jsonEx.printStackTrace();
            }
        }
    }

    public static List<BlenderObject> listBinaries() {
        if (blenderObjectList == null) {
            getList();
        }
        return blenderObjectList;
    }

    public static List<String> listVersions() {
        if (blenderObjectList == null) {
            getList();
        }
        List<String> versions = new LinkedList<>();
        for (BlenderObject blenderObject : blenderObjectList) {
            versions.add(blenderObject.getBlenderVersion());
        }
        return versions;
    }

    public static void refresh() {
        blenderObjectList = null;
    }
}
