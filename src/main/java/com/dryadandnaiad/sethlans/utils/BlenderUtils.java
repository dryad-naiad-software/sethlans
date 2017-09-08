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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.domains.blender.BlenderZip;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataServiceImpl;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collection;
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
    private static List<BlenderZip> blenderZipList = null;

    private static void getList() {

        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getResult("https://gist.githubusercontent.com/marioestrella/def9d852b3298008ae16040bbbabc524/raw/");
        if (data == null || data.isEmpty() || !data.startsWith("[")) {
            LOG.debug("Trying mirror");
            data = getJSONData.getResult("https://gitlab.com/snippets/1656456/raw");
            if (data == null || data.isEmpty() || !data.startsWith("[")) {
                LOG.debug("Unable to retrieve blenderdownload.json from internet, using local version instead.");
                data = getJSONData.getLocalResult("blenderdownload.json");
            }
        }
        LOG.debug("Retrieved JSON: \n" + data.substring(0, 100) + "...");
        if (data != null || !data.isEmpty()) {
            blenderZipList = new LinkedList<>();
            Gson gson = new Gson();


            try {
                Type collectionType = new TypeToken<Collection<BlenderZip>>() {
                }.getType();
                blenderZipList = gson.fromJson(data, collectionType);

            } catch (JsonSyntaxException jsonEx) {
                LOG.error("Error processing JSON data" + jsonEx.getMessage());
                LOG.error(Throwables.getStackTraceAsString(jsonEx));
            }
        }
    }

    public static List<BlenderZip> listBinaries() {
        if (blenderZipList == null) {
            getList();
        }
        return blenderZipList;
    }

    public static List<String> listVersions() {
        if (blenderZipList == null) {
            getList();
        }
        List<String> versions = new LinkedList<>();
        for (BlenderZip blenderZip : blenderZipList) {
            versions.add(blenderZip.getBlenderVersion());
        }
        return versions;
    }

    public static void refresh() {
        blenderZipList = null;
    }
}
