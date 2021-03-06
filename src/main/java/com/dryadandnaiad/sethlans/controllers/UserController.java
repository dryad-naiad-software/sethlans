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
import com.dryadandnaiad.sethlans.domains.info.UserInfo;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.user.NotificationsForm;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created Mario Estrella on 2/26/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @GetMapping(value = {"/username"})
    public Map getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        } else {
            return Collections.singletonMap("username", auth.getName());
        }
    }

    @GetMapping(value = {"/is_authenticated"})
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.isAuthenticated();
    }

    @GetMapping(value = {"/is_administrator"})
    public boolean isAdministrator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        return sethlansUser.getRoles().contains(Role.ADMINISTRATOR) || sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR);
    }

    @GetMapping(value = {"/is_super_administrator"})
    public boolean isSuperAdministrator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(auth.getName());
        return sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR);
    }

    @GetMapping(value = {"/prompt_pass_change"})
    public boolean promptPasswordChange() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansUserDatabaseService.findByUserName(auth.getName()).isPromptPasswordChange();
    }

    @GetMapping(value = {"/admin_added_user"})
    public boolean isUserAddedByAdmin() {
        return promptPasswordChange() && !isSecurityQuestionsSet();
    }

    @GetMapping(value = {"/is_security_questions_set"})
    public boolean isSecurityQuestionsSet() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansUserDatabaseService.findByUserName(auth.getName()).isSecurityQuestionsSet();
    }

    @GetMapping(value = {"/get_user/{username}"})
    public UserInfo getUserInfo(@PathVariable String username) {
        if (requestMatchesAuthUser(username)) {
            SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(username);
            UserInfo userToSend = new UserInfo();
            userToSend.loadUserInfo(sethlansUser);
            return userToSend;
        } else {
            return null;
        }
    }

    @PostMapping(value = {"/change_notifications"})
    public boolean changeNotifications(@RequestBody NotificationsForm notificationsForm) {
        if (SethlansQueryUtils.getMode() != SethlansMode.NODE) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(username);
            sethlansUser.setVideoEncodingEmailNotifications(notificationsForm.isVideoEncodingEmailNotifications());
            sethlansUser.setProjectEmailNotifications(notificationsForm.isProjectEmailNotifications());
            sethlansUser.setSystemEmailNotifications(notificationsForm.isSystemEmailNotifications());
            sethlansUser.setNodeEmailNotifications(notificationsForm.isNodeEmailNotifications());
            sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
            return true;
        }
        return false;
    }

    @PostMapping(value = {"/change_email/"})
    public boolean changeEmail(@RequestParam String email) {
        if (SethlansQueryUtils.getMode() != SethlansMode.NODE) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(username);
            // TODO email verification
            sethlansUser.setEmail(email);
            sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
            return true;
        }
        return false;

    }



    @PostMapping(value = {"/change_security_questions"})
    public boolean changeSecurityQuestions(@RequestBody SethlansUserChallenge[] userChallenges) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        String username = auth.getName();
        List<SethlansUserChallenge> challengeList = new ArrayList<>(Arrays.asList(userChallenges));
        if (challengeList.size() < 3) {
            return false;
        }
        for (SethlansUserChallenge sethlansUserChallenge : challengeList) {
            if (sethlansUserChallenge.getChallenge().isEmpty() || sethlansUserChallenge.getResponse().isEmpty()) {
                return false;
            }
            sethlansUserChallenge.setResponseUpdated(true);
        }
        SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
        user.setChallengeList(challengeList);
        user.setSecurityQuestionsSet(true);
        sethlansUserDatabaseService.saveOrUpdate(user);
        return true;
    }


    @PostMapping(value = {"/change_password/"})
    public boolean changePassword(@RequestParam String passToCheck, @RequestParam String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        String username = auth.getName();
        // TODO password verification
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
        if (encoder.matches(passToCheck, user.getPassword())) {
            LOG.debug("Updating password for " + user.getUsername());
            user.setPasswordUpdated(true);
            user.setPassword(newPassword);
            user.setPromptPasswordChange(false);
            sethlansUserDatabaseService.saveOrUpdate(user);
            return true;
        } else {
            return false;
        }
    }

    private boolean requestMatchesAuthUser(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getName().equals(username);
    }

    @GetMapping(value = {"/user_challenge_list"})
    public List<SethlansUserChallenge> getChallengeQuestion(@RequestParam String username) {
        SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
        if (user != null) {
            user.setTokenList(new ArrayList<>());
            sethlansUserDatabaseService.saveOrUpdate(user);
            List<SethlansUserChallenge> listToSend = new ArrayList<>(user.getChallengeList());
            for (int i = 0; i < listToSend.size(); i++) {
                listToSend.get(i).setResponse(Integer.toString(i));
            }
            Collections.shuffle(listToSend);
            return listToSend;
        }
        return null;
    }

    @PostMapping(value = {"/submit_challenge_response"})
    public String verifyResponse(@RequestParam String username, @RequestParam int key, @RequestParam String submittedResponse) {
        SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
        if (user != null) {
            String storedResponse = user.getChallengeList().get(key).getResponse();
            if (bCryptPasswordEncoder.matches(submittedResponse, storedResponse)) {
                LOG.debug("Valid response.");
                String token = UUID.randomUUID().toString();
                user.getTokenList().add(token);
                sethlansUserDatabaseService.saveOrUpdate(user);
                return token;
            } else {
                LOG.debug("Invalid response.");
                return "invalid";
            }
        }
        return null;
    }

    @PostMapping(value = {"/reset_password"})
    public boolean resetPassword(@RequestParam String username, @RequestParam(value = "tokens") String[] tokens, @RequestParam String newPassword) {
        SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
        if (user != null) {
            List<String> tokensToCompare = Arrays.asList(tokens);
            if (user.getTokenList().containsAll(tokensToCompare)) {
                LOG.debug("Tokens Valid");
                user.setPassword(newPassword);
                user.setPasswordUpdated(true);
                user.setPromptPasswordChange(false);
                user.setSecurityQuestionsSet(true);
                user.setTokenList(new ArrayList<>());
                sethlansUserDatabaseService.saveOrUpdate(user);
                return true;
            } else {
                LOG.debug("Invalid Tokens presented");
                return false;
            }
        }
        return false;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }

    @Autowired
    public void setbCryptPasswordEncoder(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
}
