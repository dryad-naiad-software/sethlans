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

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.devices.ScanGPU;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.services.SethlansManagerService;
import com.dryadandnaiad.sethlans.services.SetupService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * File created by Mario Estrella on 5/24/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Profile("SETUP")
@RestController
@RequestMapping("/api/v1/setup")
@Slf4j
public class SetupController {
    private final SetupService setupService;
    private final SethlansManagerService sethlansManagerService;

    public SetupController(SetupService setupService, SethlansManagerService sethlansManagerService) {
        this.setupService = setupService;
        this.sethlansManagerService = sethlansManagerService;
    }

    @GetMapping("/get_setup")
    public SetupForm prePopulatedSetupForm() {
        var port = ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT);
        return SetupForm.builder()
                .ipAddress(QueryUtils.getIP())
                .port(port)
                .logLevel(LogLevel.INFO)
                .appURL("https://" + QueryUtils.getHostname().toLowerCase() + ":" + port + "/")
                .availableTypes(QueryUtils.getAvailableTypes())
                .availableGPUs(ScanGPU.listDevices())
                .blenderVersions(BlenderUtils.availableBlenderVersions())
                .build();

    }


    @PostMapping("/submit")
    public ResponseEntity<Void> completeSetup(@RequestBody SetupForm setupForm) {
        if (setupService.saveSetupSettings(setupForm)) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/restart")
    public ResponseEntity<Void> restartSethlans() {
        sethlansManagerService.restart();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/shutdown")
    public ResponseEntity<Void> shutdownSethlans() {
        sethlansManagerService.shutdown();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
