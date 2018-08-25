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
import com.dryadandnaiad.sethlans.domains.info.Log;
import com.dryadandnaiad.sethlans.domains.info.RoleInfo;
import com.dryadandnaiad.sethlans.domains.info.SethlansSettings;
import com.dryadandnaiad.sethlans.domains.info.UserInfo;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.services.system.SethlansLogRetrievalService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.writeProperty;
/**
 * Created Mario Estrella on 3/2/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/management")
public class AdminController {
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private SethlansLogRetrievalService sethlansLogRetrievalService;
    private SethlansManagerService sethlansManagerService;
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Value("${sethlans.configDir}")
    private String configDir;

    @Value("${logging.level.com.dryadandnaiad.sethlans}")
    private String logLevel;

    @GetMapping(value = "/restart")
    public void restart() {
        sethlansManagerService.restart();
        LOG.debug("Restarting Sethlans...");
    }

    @GetMapping(value = "/shutdown")
    public void shutdown() {
        sethlansManagerService.shutdown();
        LOG.debug("Shutting down Sethlans...");
    }

    @GetMapping(value = "/user_list")
    public List<UserInfo> sethlansUserList() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        List<SethlansUser> sethlansUsers;
        if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
            sethlansUsers = sethlansUserDatabaseService.listAll();
        } else {
            sethlansUsers = sethlansUserDatabaseService.excludeSuperAdministrators();
        }

        List<UserInfo> userInfoList = new ArrayList<>();
        for (SethlansUser sethlansUser : sethlansUsers) {
            UserInfo userToSend = new UserInfo();
            userToSend.setUsername(sethlansUser.getUsername());
            userToSend.setActive(sethlansUser.isActive());
            userToSend.setRoles(sethlansUser.getRoles());
            userToSend.setEmail(sethlansUser.getEmail());
            userToSend.setId(sethlansUser.getId());
            userToSend.setLastUpdated(sethlansUser.getLastUpdated());
            userToSend.setDateCreated(sethlansUser.getDateCreated());
            userInfoList.add(userToSend);
        }
        return userInfoList;
    }


    @GetMapping(value = {"/get_logs"})
    public List<Log> getSethlansLogs() {
        return sethlansLogRetrievalService.sethlansLogList();
    }

    @GetMapping(value = "/current_settings")
    public SethlansSettings sethlansSettingsInfo() {
        return SethlansQueryUtils.getSettings();
    }

    @PostMapping(value = "/update_settings")
    public boolean updateSettings(@RequestBody SethlansSettings sethlansSettingsUpdate) {
        SethlansSettings currentSettings = SethlansQueryUtils.getSettings();
        if (!sethlansSettingsUpdate.equals(currentSettings)) {
            LOG.debug(sethlansSettingsUpdate.toString());
            LOG.debug(currentSettings.toString());
            LOG.info("Configuration Change Requested");
            writeProperty(SethlansConfigKeys.HTTPS_PORT, sethlansSettingsUpdate.getHttpsPort());
            writeProperty(SethlansConfigKeys.SETHLANS_IP, sethlansSettingsUpdate.getSethlansIP());
            writeProperty(SethlansConfigKeys.LOG_LEVEL, sethlansSettingsUpdate.getLogLevel());
            return true;
        }
        return false;
    }

    @PostMapping(value = {"/change_roles/{id}"})
    public boolean changeRoles(@PathVariable Long id, @RequestBody List<RoleInfo> roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SethlansUser sethlansUser = sethlansUserDatabaseService.getById(id);
        SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        List<Role> roleList = new ArrayList<>();
        for (RoleInfo role : roles) {
            if (role.isActive()) {
                Role theRole = role.getRole();
                if (!requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR) && theRole.equals(Role.SUPER_ADMINISTRATOR)) {
                    break;
                }
                roleList.add(theRole);
            }
        }
        sethlansUser.setRoles(roleList);
        sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
        return true;
    }

    @PostMapping(value = {"/change_email/"})
    public boolean changeEmail(@RequestParam String id, @RequestParam String email) {
        SethlansUser sethlansUser = sethlansUserDatabaseService.getById(Long.valueOf(id));
        // TODO email verification
        sethlansUser.setEmail(email);
        sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
        return true;
    }

    @GetMapping(value = {"/change_get_started_wizard"})
    public void changeGetStartedOnStart(@RequestParam boolean value) {
        writeProperty(SethlansConfigKeys.GETTING_STARTED, Boolean.toString(value));
    }

    @PostMapping(value = {"/change_password/"})
    public boolean changePassword(@RequestParam String id, @RequestParam String passToCheck, @RequestParam String newPassword) {
        // TODO password verification
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        SethlansUser user = sethlansUserDatabaseService.getById(Long.valueOf(id));
        if (encoder.matches(passToCheck, user.getPassword())) {
            LOG.debug("Updating password for " + user.getUsername());
            user.setPasswordUpdated(true);
            user.setPassword(newPassword);
            sethlansUserDatabaseService.saveOrUpdate(user);
            return true;
        } else {
            return false;
        }
    }

    @GetMapping(value = {"/get_user/{id}"})
    public UserInfo getUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        SethlansUser sethlansUser;
        if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
            sethlansUser = sethlansUserDatabaseService.getById(id);
        } else {
            sethlansUser = sethlansUserDatabaseService.excludeSuperUsersById(id);
        }
        UserInfo userToSend = new UserInfo();
        userToSend.setUsername(sethlansUser.getUsername());
        userToSend.setActive(sethlansUser.isActive());
        userToSend.setRoles(sethlansUser.getRoles());
        userToSend.setEmail(sethlansUser.getEmail());
        List<SethlansUserChallenge> filteredList = new ArrayList<>();
        for (SethlansUserChallenge sethlansUserChallenge : sethlansUser.getChallengeList()) {
            SethlansUserChallenge toSend = new SethlansUserChallenge();
            toSend.setChallenge(sethlansUserChallenge.getChallenge());
            filteredList.add(toSend);
        }
        userToSend.setUserChallengeList(filteredList);
        userToSend.setId(sethlansUser.getId());
        userToSend.setLastUpdated(sethlansUser.getLastUpdated());
        userToSend.setDateCreated(sethlansUser.getDateCreated());
        return userToSend;
    }

    @GetMapping(value = {"/get_roles/{id}"})
    public List<RoleInfo> getRoles(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        EnumSet<Role> allRoles;
        if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
            allRoles = EnumSet.allOf(Role.class);
        } else {
            allRoles = EnumSet.of(Role.ADMINISTRATOR, Role.USER);
        }
        SethlansUser sethlansUser = sethlansUserDatabaseService.getById(id);
        List<RoleInfo> roleInfoList = new ArrayList<>();
        for (Role role : allRoles) {
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setRole(role);
            if (sethlansUser.getRoles().contains(role)) {
                roleInfo.setActive(true);
            } else {
                roleInfo.setActive(false);
            }
            roleInfoList.add(roleInfo);
        }
        return roleInfoList;
    }

    @GetMapping(value = {"/activate_user/{id}"})
    public void activateUser(@PathVariable Long id) {
        SethlansUser sethlansUser = sethlansUserDatabaseService.getById(id);
        if (!sethlansUser.isActive()) {
            sethlansUser.setActive(true);
            sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
        }
    }

    @GetMapping(value = {"/deactivate_user/{id}"})
    public void deactivateUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (sethlansUserDatabaseService.listAll().size() > 1) {
            boolean authorized = false;
            SethlansUser sethlansUser = sethlansUserDatabaseService.getById(id);
            SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                authorized = true;
            } else if (requestingUser.getRoles().contains(Role.ADMINISTRATOR) && !sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                authorized = true;
            }
            if (sethlansUser.isActive() && !sethlansUser.getUsername().equals(auth.getName()) && authorized) {
                sethlansUser.setActive(false);
                sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
            }
        }
    }

    @GetMapping("/delete_user/{id}")
    public void deleteUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SethlansUser sethlansUser = sethlansUserDatabaseService.getById(id);
        if (sethlansUserDatabaseService.listAll().size() > 1 && !sethlansUser.getUsername().equals(auth.getName())) {
            SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
            boolean authorized = false;
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                authorized = true;
            } else if (requestingUser.getRoles().contains(Role.ADMINISTRATOR) && !sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                authorized = true;
            }
            if (authorized) {
                sethlansUserDatabaseService.delete(id);
            }
        }
    }

    @PostMapping("/add_user")
    public boolean addUser(@RequestBody SethlansUser user) {
        if (user != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean authorized = false;
            SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR) || requestingUser.getRoles().contains(Role.ADMINISTRATOR)) {
                authorized = true;
            }
            if (authorized) {
                LOG.debug("Adding new user...");
                if (sethlansUserDatabaseService.checkifExists(user.getUsername())) {
                    LOG.debug("User " + user.getUsername() + " already exists!");
                    return false;
                }
                if (!requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                    user.getRoles().remove(Role.SUPER_ADMINISTRATOR);
                }
                user.setPasswordUpdated(true);
                user.setActive(false);
                sethlansUserDatabaseService.saveOrUpdate(user);
                LOG.debug("Saving " + user.toString() + " to database.");
                return true;
            }
            return false;

        } else {
            return false;
        }
    }

    @GetMapping("/requesting_user")
    public UserInfo requestingUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        UserInfo userToSend = new UserInfo();
        userToSend.setUsername(sethlansUser.getUsername());
        userToSend.setActive(sethlansUser.isActive());
        userToSend.setRoles(sethlansUser.getRoles());
        userToSend.setEmail(sethlansUser.getEmail());
        userToSend.setId(sethlansUser.getId());
        userToSend.setLastUpdated(sethlansUser.getLastUpdated());
        userToSend.setDateCreated(sethlansUser.getDateCreated());
        return userToSend;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }

    @Autowired
    public void setSethlansManagerService(SethlansManagerService sethlansManagerService) {
        this.sethlansManagerService = sethlansManagerService;
    }

    @Autowired
    public void setSethlansLogRetrievalService(SethlansLogRetrievalService sethlansLogRetrievalService) {
        this.sethlansLogRetrievalService = sethlansLogRetrievalService;
    }


}
