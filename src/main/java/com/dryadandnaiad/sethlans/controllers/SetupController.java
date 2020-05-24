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
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
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
public class SetupController {
    private final UserRepository userRepository;

    public SetupController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity saveForm(@RequestBody SetupForm setupForm) {
        ConfigUtils.writeProperty(ConfigKeys.MODE, setupForm.getMode().name());
        PropertiesUtils.writeMailSettings(setupForm.getMailSettings());
        ConfigUtils.writeProperty(ConfigKeys.SETHLANS_IP, setupForm.getIpAddress());
        ConfigUtils.writeProperty(ConfigKeys.HTTPS_PORT, setupForm.getPort());
        ConfigUtils.writeProperty(ConfigKeys.SETHLANS_URL, setupForm.getAppURL());
        userRepository.save(setupForm.getUser());
        return new ResponseEntity(HttpStatus.CREATED);
    }
}
