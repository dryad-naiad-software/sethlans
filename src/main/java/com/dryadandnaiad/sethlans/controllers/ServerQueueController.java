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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;

/**
 * File created by Mario Estrella on 6/17/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/server_queue")
public class ServerQueueController {
    private final BlenderArchiveRepository blenderArchiveRepository;
    private final NodeRepository nodeRepository;

    public ServerQueueController(BlenderArchiveRepository blenderArchiveRepository, NodeRepository nodeRepository) {
        this.blenderArchiveRepository = blenderArchiveRepository;
        this.nodeRepository = nodeRepository;
    }


    @GetMapping(value = "/latest_blender_archive",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getLatestBlenderArchive(@RequestParam("system-id") String systemID,
                                   @RequestParam("os") String os) {
        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            var selectedArchive = getLatestBlenderArchive(os);
            if (selectedArchive != null) {
                try {
                    InputStream inputStream = new BufferedInputStream(new
                            FileInputStream(new File(selectedArchive.getBlenderFile())));
                    return IOUtils.toByteArray(inputStream);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    log.error(Throwables.getStackTraceAsString(e));
                    return null;
                }
            }
        }
        return null;
    }

    @GetMapping("/latest_blender_archive_md5")
    public String getLatestBlenderArchiveMD5(@RequestParam("system-id") String systemID,
                                             @RequestParam("os") String os) {
        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            var selectedArchive = getLatestBlenderArchive(os);
            if (selectedArchive != null) {
                return selectedArchive.getBlenderFileMd5();
            }
            return null;
        }
        return null;
    }

    private BlenderArchive getLatestBlenderArchive(String os) {
        var blenderArchives = blenderArchiveRepository.findAllByDownloadedIsTrueAndBlenderOSEquals(OS.valueOf(os));
        var blenderVersions = new ArrayList<String>();
        for (BlenderArchive blenderArchive : blenderArchives) {
            blenderVersions.add(blenderArchive.getBlenderVersion());
            blenderVersions.sort(new AlphaNumericComparator());
            var selectedArchive = blenderArchiveRepository.findBlenderBinaryByBlenderVersionEqualsAndBlenderOSEquals
                    (blenderVersions.get(0), OS.valueOf(os));
            return selectedArchive.orElse(null);
        }
        return null;
    }

}
