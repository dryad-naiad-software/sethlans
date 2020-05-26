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

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.repositories.BlenderBinaryRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final BlenderBinaryRepository blenderBinaryRepository;

    public SetupServiceImpl(UserRepository userRepository, BlenderBinaryRepository blenderBinaryRepository) {
        this.userRepository = userRepository;
        this.blenderBinaryRepository = blenderBinaryRepository;
    }

    @Override
    public boolean saveSetupSettings(SetupForm setupForm) {
        try {
            PropertiesUtils.writeMainSettings(setupForm);
            PropertiesUtils.writeDirectories(setupForm.getMode());
            if (setupForm.getMode().equals(SethlansMode.DUAL) || setupForm.getMode().equals(SethlansMode.SERVER)) {
                PropertiesUtils.writeServerSettings(setupForm);
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
            System.out.println("Test1");
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
        userRepository.save(setupForm.getUser());
        return true;

    }
}