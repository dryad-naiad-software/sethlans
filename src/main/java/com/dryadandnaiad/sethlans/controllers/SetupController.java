/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUserChallenge;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.setup.SetupForm;
import com.dryadandnaiad.sethlans.services.config.SaveSetupConfigService;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {
    private static final Logger LOG = LoggerFactory.getLogger(SetupController.class);
    private SaveSetupConfigService saveSetupConfigService;
    private SethlansUserDatabaseService sethlansUserDatabaseService;


    @PostMapping("/submit")
    public boolean submit(@RequestBody SetupForm setupForm) {
        LOG.debug("Submitting Setup Form...");
        if (setupForm != null) {
            LOG.debug(setupForm.toString());
            if (setupForm.getUser().getPassword().isEmpty()
                    || setupForm.getUser().getUsername().isEmpty()
                    || setupForm.getUser().getChallengeList().size() == 0) {
                return false;
            }
            if (setupForm.getMode() != SethlansMode.NODE && setupForm.getUser().getEmail().isEmpty()) {
                return false;
            }
            for (SethlansUserChallenge sethlansUserChallenge : setupForm.getUser().getChallengeList()) {
                if (sethlansUserChallenge.getChallenge().isEmpty() || sethlansUserChallenge.getResponse().isEmpty()) {
                    return false;
                }
                if (!sethlansUserChallenge.isResponseUpdated()) {
                    sethlansUserChallenge.setResponseUpdated(true);
                }

            }
            setupForm.getUser().setPromptPasswordChange(false);
            saveSetupConfigService.saveSetupSettings(setupForm);
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("/self_register")
    public boolean selfRegister(@RequestBody SethlansUser user) {
        if (user != null) {
            LOG.debug("Registering new user...");
            if (sethlansUserDatabaseService.checkIfExists(user.getUsername())) {
                LOG.debug("User " + user.getUsername() + " already exists!");
                return false;
            }
            if (SethlansQueryUtils.getMode() != SethlansMode.NODE && user.getEmail().isEmpty()) {
                return false;
            }
            if (user.getPassword().isEmpty()
                    || user.getUsername().isEmpty()
                    || user.getChallengeList().size() == 0) {
                return false;
            }

            for (SethlansUserChallenge sethlansUserChallenge : user.getChallengeList()) {
                if (sethlansUserChallenge.getChallenge().isEmpty() || sethlansUserChallenge.getResponse().isEmpty()) {
                    return false;
                }
                if (!sethlansUserChallenge.isResponseUpdated()) {
                    sethlansUserChallenge.setResponseUpdated(true);
                }

            }

            user.setPasswordUpdated(true);
            user.setActive(false);
            user.setPromptPasswordChange(false);
            user.setRoles(Collections.singletonList(Role.USER));
            sethlansUserDatabaseService.saveOrUpdate(user);
            LOG.debug("Saving " + user.toString() + " to database.");
            return true;
        } else {
            return false;
        }
    }


    @Autowired
    public void setSaveSetupConfigService(SaveSetupConfigService saveSetupConfigService) {
        this.saveSetupConfigService = saveSetupConfigService;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }


}
