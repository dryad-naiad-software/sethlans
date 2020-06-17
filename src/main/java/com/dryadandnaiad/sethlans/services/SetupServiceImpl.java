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

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File created by Mario Estrella on 5/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Slf4j
public class SetupServiceImpl implements SetupService {
    private final UserRepository userRepository;
    private final BlenderArchiveRepository blenderArchiveRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SetupServiceImpl(UserRepository userRepository, BlenderArchiveRepository blenderArchiveRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SethlansManagerService sethlansManagerService) {
        this.blenderArchiveRepository = blenderArchiveRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public boolean saveSetupSettings(SetupForm setupForm) {
        try {
            PropertiesUtils.writeSetupSettings(setupForm);
            PropertiesUtils.writeDirectories(setupForm.getMode());
            if (setupForm.getMode().equals(SethlansMode.DUAL) || setupForm.getMode().equals(SethlansMode.SERVER)) {
                blenderArchiveRepository.save(BlenderArchive.builder()
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
            return false;
        }
        var user = setupForm.getUser();
        user.setUsername(user.getUsername().toLowerCase());
        user.setRoles(Stream.of(Role.SUPER_ADMINISTRATOR).collect(Collectors.toSet()));
        user.setActive(true);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }
}
