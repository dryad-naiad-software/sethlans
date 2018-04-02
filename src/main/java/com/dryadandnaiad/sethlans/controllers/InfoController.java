/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/info")
public class InfoController {
    private static final Logger LOG = LoggerFactory.getLogger(InfoController.class);
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private List<String> blenderVersions = BlenderUtils.listVersions();


    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @Value("${sethlans.mode}")
    private SethlansMode mode;

    @Value("${sethlans.computeMethod}")
    private ComputeType computeType;

    @GetMapping(value = {"/first_time"})
    public boolean isFirstTime() {
        return firstTime;
    }

    @GetMapping(value = {"/installed_blender_versions"})
    public Set<String> getInstalledBlenderVersions() {
        return blenderBinaryDatabaseService.installedBlenderVersions();
    }


    @GetMapping(value = {"/version"})
    public String getVersion() {
        return SethlansUtils.getVersion();
    }

    @GetMapping(value = {"/blender_versions"})
    public List<String> getBlenderVersions() {
        return blenderVersions;
    }

    @GetMapping(value = {"/root_directory"})
    public String getRootDirectory() {
        if (firstTime) {
            return System.getProperty("user.home") + File.separator + ".sethlans";
        } else {
            return null;
        }
    }


    @GetMapping(value = {"/sethlans_port"})
    public String getHttpsPort() {
        if (firstTime) {
            return "7443";
        } else {
            return SethlansUtils.getPort();
        }
    }




    @GetMapping(value = {"/sethlans_mode"})
    public String getSethlansMode() {
        return mode.toString();
    }

    @GetMapping(value = {"/sethlans_ip"})
    public String getSethlansIPAddress() {
        return SethlansUtils.getIP();
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }


}
