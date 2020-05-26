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

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.repositories.BlenderBinaryRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final UserRepository userRepository;
    private final BlenderBinaryRepository blenderBinaryRepository;

    public SetupController(UserRepository userRepository, BlenderBinaryRepository blenderBinaryRepository) {
        this.userRepository = userRepository;
        this.blenderBinaryRepository = blenderBinaryRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity saveForm(@RequestBody SetupForm setupForm) {
        try {
            ConfigUtils.writeProperty(ConfigKeys.MODE, setupForm.getMode().name());
            ConfigUtils.writeProperty(ConfigKeys.SETHLANS_IP, setupForm.getIpAddress());
            ConfigUtils.writeProperty(ConfigKeys.HTTPS_PORT, setupForm.getPort());
            ConfigUtils.writeProperty(ConfigKeys.SETHLANS_URL, setupForm.getAppURL());
            if (setupForm.getMode().equals(SethlansMode.DUAL) || setupForm.getMode().equals(SethlansMode.SERVER)) {
                blenderBinaryRepository.save(BlenderBinary.builder()
                        .blenderOS(QueryUtils.getOS())
                        .downloaded(false)
                        .blenderVersion(setupForm.getServerSettings().getBlenderVersion())
                        .build());
            }
            if (setupForm.getMode().equals(SethlansMode.DUAL) || setupForm.getMode().equals(SethlansMode.NODE)) {
                PropertiesUtils.writeNodeSettings(setupForm.getNodeSettings());
            }
            PropertiesUtils.writeMailSettings(setupForm.getMailSettings());

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        userRepository.save(setupForm.getUser());
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
