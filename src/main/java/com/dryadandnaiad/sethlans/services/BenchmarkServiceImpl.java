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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * File created by Mario Estrella on 6/17/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Slf4j
public class BenchmarkServiceImpl implements BenchmarkService {

    @Override
    @Async
    public void processBenchmarkRequest(Server server) {
        try {
            var nodeSystemID = ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID);
            var os = QueryUtils.getOS().getName();
            var url = new URL("https://" + server.getIpAddress() + ":" + server.getNetworkPort() +
                    "/api/v1/server_queue/latest_blender_archive?system-id=" + nodeSystemID + "&os=" + os);

            var blenderArchiveJSON = NetworkUtils.getJSONFromURL(url);
            var objectMapper = new ObjectMapper();
            var blenderArchive = objectMapper.readValue(blenderArchiveJSON, new TypeReference<BlenderArchive>() {
            });
        } catch (MalformedURLException | JsonProcessingException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }

    }

    @Override
    public boolean benchmarkStatus(Server server) {
        return false;
    }
}
