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

import com.dryadandnaiad.sethlans.domains.Blender;
import com.dryadandnaiad.sethlans.services.GetRawDataService;
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
    private static List<Blender> blenderList = null;

    private static void getList() {

        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getResult("https://gist.githubusercontent.com/marioestrella/def9d852b3298008ae16040bbbabc524/raw/");
        LOG.debug("Retrieved JSON: \n" + data);
        if (data != null || !data.isEmpty()) {
            blenderList = new LinkedList<>();

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
                    Blender blenderObject = new Blender(version, windows32, windows64, macOS, linux32, linux64);
                    blenderList.add(blenderObject);
                }
            } catch (JSONException jsonEx) {
                LOG.error("Error processing JSON data" + jsonEx.getMessage());
                jsonEx.printStackTrace();
            }
        }
    }

    public static List<Blender> listBinaries() {
        if (blenderList == null) {
            getList();
        }
        return blenderList;
    }

    public static List<String> listVersions() {
        if (blenderList == null) {
            getList();
        }
        List<String> versions = new LinkedList<>();
        for (Blender blender : blenderList) {
            versions.add(blender.getBlenderVersion());
        }
        return versions;
    }
}
