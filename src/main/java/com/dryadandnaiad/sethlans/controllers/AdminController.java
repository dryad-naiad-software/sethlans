/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.domains.info.*;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.services.mail.SethlansEmailService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.services.system.SethlansLogManagementService;
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

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.writeProperty;
import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.serveFile;

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
    private SethlansLogManagementService sethlansLogManagementService;
    private SethlansManagerService sethlansManagerService;
    private SethlansNotificationService sethlansNotificationService;
    private SethlansEmailService sethlansEmailService;


    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Value("${sethlans.configDir}")
    private String configDir;

    @Value("${logging.level.com.dryadandnaiad.sethlans}")
    private String logLevel;

    @GetMapping(value = "/restart")
    public void restart() {
        String message = "Restarting Sethlans";
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SYSTEM, message);
        sethlansNotification.setMailable(true);
        sethlansNotification.setSubject("Restart Initiated");
        sethlansNotificationService.sendNotification(sethlansNotification);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sethlansManagerService.restart();
    }

    @GetMapping(value = "/shutdown")
    public void shutdown() {
        String message = "Shutting down Sethlans";
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SYSTEM, message);
        sethlansNotification.setMailable(true);
        sethlansNotification.setSubject("Shutdown Initiated");
        sethlansNotificationService.sendNotification(sethlansNotification);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sethlansManagerService.shutdown();
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
            userToSend.loadUserInfo(sethlansUser);
            userInfoList.add(userToSend);
        }
        return userInfoList;
    }


    @GetMapping(value = {"/get_logs"})
    public List<Log> getSethlansLogs() {
        return sethlansLogManagementService.sethlansLogList();
    }

    @GetMapping(value = {"/get_latest_log_archives"})
    public void getLatestLogArchiveFile(HttpServletResponse response){
        File zipFile = sethlansLogManagementService.retrieveLogFiles();
        if (zipFile != null) {
            serveFile(zipFile, response);
        }
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
            writeProperty(SethlansConfigKeys.SETHLANS_URL, sethlansSettingsUpdate.getSethlansURL());
            writeProperty(SethlansConfigKeys.GETTING_STARTED, Boolean.toString(sethlansSettingsUpdate.isGetStarted()));
            writeProperty(SethlansConfigKeys.MAIL_SERVER_CONFIGURED, Boolean.toString(sethlansSettingsUpdate.isConfigureMail()));
            if (sethlansSettingsUpdate.isConfigureMail()) {
                writeProperty(SethlansConfigKeys.MAIL_HOST, sethlansSettingsUpdate.getMailSettings().getMailHost());
                writeProperty(SethlansConfigKeys.MAIL_PORT, sethlansSettingsUpdate.getMailSettings().getMailPort());
                writeProperty(SethlansConfigKeys.MAIL_REPLYTO, sethlansSettingsUpdate.getMailSettings().getReplyToAddress());
                writeProperty(SethlansConfigKeys.MAIL_USE_AUTH, Boolean.toString(sethlansSettingsUpdate.getMailSettings().isSmtpAuth()));
                if (sethlansSettingsUpdate.getMailSettings().isSmtpAuth()) {
                    writeProperty(SethlansConfigKeys.MAIL_USER, sethlansSettingsUpdate.getMailSettings().getUsername());
                    writeProperty(SethlansConfigKeys.MAIL_PASS, sethlansSettingsUpdate.getMailSettings().getPassword());
                }
                writeProperty(SethlansConfigKeys.MAIL_SSL_ENABLE, Boolean.toString(sethlansSettingsUpdate.getMailSettings().isSslEnabled()));
                writeProperty(SethlansConfigKeys.MAIL_TLS_ENABLE, Boolean.toString(sethlansSettingsUpdate.getMailSettings().isStartTLSEnabled()));
                writeProperty(SethlansConfigKeys.MAIL_TLS_REQUIRED, Boolean.toString(sethlansSettingsUpdate.getMailSettings().isStartTLSRequired()));
            }
            return true;
        }
        return false;
    }

    @PostMapping(value = {"/change_roles/{id}"})
    public boolean changeRoles(@PathVariable Long id, @RequestBody List<RoleInfo> roles) {
        if (sethlansUserDatabaseService.tableSize() > 1) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            SethlansUser sethlansUser = sethlansUserDatabaseService.getById(id);
            SethlansUser requestingUser = sethlansUserDatabaseService.findByUserName(auth.getName());
            if (requestingUser.equals(sethlansUser) && sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR) && sethlansUserDatabaseService.numberOfSuperAdministrators() == 1) {
                return false;
            }
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
        return false;
    }

    @PostMapping(value = {"/change_email/"})
    public boolean changeEmail(@RequestParam String id, @RequestParam String email) {
        SethlansUser sethlansUser = sethlansUserDatabaseService.getById(Long.valueOf(id));
        // TODO email verification
        sethlansUser.setEmail(email);
        sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
        return true;
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
        userToSend.loadUserInfo(sethlansUser);
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
            if (!sethlansUser.isWelcomeEmailSent()) {
                sethlansUser.setWelcomeEmailSent(sethlansEmailService.sendWelcomeEmail(sethlansUser));
            }
            sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
        }
    }

    @GetMapping(value = {"/deactivate_user/{id}"})
    public void deactivateUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (sethlansUserDatabaseService.tableSize() > 1) {
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
        if (sethlansUserDatabaseService.tableSize() > 1 && !sethlansUser.getUsername().equals(auth.getName())) {
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
                LOG.info("Adding new user...");
                if (sethlansUserDatabaseService.checkIfExists(user.getUsername())) {
                    LOG.warn("User " + user.getUsername() + " already exists!");
                    return false;
                }
                if (!requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                    user.getRoles().remove(Role.SUPER_ADMINISTRATOR);
                }
                user.setPasswordUpdated(true);
                user.setSecurityQuestionsSet(false);
                user.setActive(false);
                user.enableAllNotifications();
                user.setPromptPasswordChange(true);
                user.setWelcomeEmailSent(false);
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
        userToSend.loadUserInfo(sethlansUser);
        return userToSend;
    }

    @GetMapping("/systeminfo")
    public SethlansSystemInfo getSystemInfo() {
        SethlansSystemInfo systemInfo = new SethlansSystemInfo();
        systemInfo.setJavaVersion(System.getProperty("java.version"));
        systemInfo.setJvmFreeMem(SethlansQueryUtils.getJVMFreeMemory());
        systemInfo.setJvmMaxMem(SethlansQueryUtils.getJVMMaxMemory());
        systemInfo.setJvmTotalMem(SethlansQueryUtils.getJVMTotalMemory());
        systemInfo.setJvmUsedMem(SethlansQueryUtils.getJVMUsedMemory());
        systemInfo.setPercentageUsedMem(SethlansQueryUtils.getJVMPercentageUsed());
        systemInfo.setCpuUsage(SethlansQueryUtils.getCPUUsage());
        return systemInfo;
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
    public void setSethlansLogManagementService(SethlansLogManagementService sethlansLogManagementService) {
        this.sethlansLogManagementService = sethlansLogManagementService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }

    @Autowired
    public void setSethlansEmailService(SethlansEmailService sethlansEmailService) {
        this.sethlansEmailService = sethlansEmailService;
    }
}
