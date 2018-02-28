package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.domains.info.UserInfo;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @GetMapping(value = {"/username"})
    public String getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();

    }

    @GetMapping(value = {"/get_user/{username}"})
    public UserInfo getUserInfo(@PathVariable String username) {
        SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(username);
        UserInfo userToSend = new UserInfo();
        userToSend.setUsername(sethlansUser.getUsername());
        userToSend.setActive(sethlansUser.isActive());
        userToSend.setRoles(sethlansUser.getRoles());
        userToSend.setEmail(sethlansUser.getEmail());
        return userToSend;
    }

    @PostMapping(value = {"/pass_change"})
    public boolean changePassword(@RequestParam("username") String username, @RequestParam("passToCheck") String passToCheck, @RequestParam String newPassword) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        SethlansUser user = sethlansUserDatabaseService.findByUserName(username);
        if (encoder.matches(passToCheck, user.getPassword())) {
            user.setPasswordUpdated(true);
            user.setPassword(newPassword);
            sethlansUserDatabaseService.saveOrUpdate(user);
            return true;
        } else {
            return false;
        }
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
