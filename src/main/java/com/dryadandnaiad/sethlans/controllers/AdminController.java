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

import com.dryadandnaiad.sethlans.converters.UserToUserQuery;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.query.UserQuery;
import com.dryadandnaiad.sethlans.models.user.SethlansUser;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.services.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.UserUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File created by Mario Estrella on 6/14/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/v1/management")
@Profile({"SERVER", "NODE", "DUAL"})
public class AdminController {
    private final SethlansManagerService sethlansManagerService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserToUserQuery userToUserQuery;

    public AdminController(SethlansManagerService sethlansManagerService, UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder, UserToUserQuery userToUserQuery) {
        this.sethlansManagerService = sethlansManagerService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userToUserQuery = userToUserQuery;
    }

    @GetMapping("/get_user")
    public UserQuery getUser(@RequestParam String userid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            var requestingUser = userRepository.findUserByUsername(auth.getName()).get();
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR) || requestingUser.getRoles().contains(Role.ADMINISTRATOR)) {
                if (userRepository.findUserByUserID(userid).isPresent()) {
                    return userToUserQuery.convert(userRepository.findUserByUserID(userid).get());
                }
            }
        }

        return null;
    }

    @GetMapping("/get_current_user")
    public UserQuery getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            return userToUserQuery.convert(userRepository.findUserByUsername(auth.getName()).get());
        }
        return null;
    }

    @GetMapping("/is_authenticated")
    public Map<String, Boolean> isFirstTime(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var authd = false;
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            authd = true;
        }
        var authenticated = new HashMap<String, Boolean>();
        authenticated.put("authenticated", authd);
        return authenticated;
    }


    @GetMapping("/user_list")
    public List<UserQuery> getUserList() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            var requestingUser = userRepository.findUserByUsername(auth.getName()).get();
            List<SethlansUser> sethlansUserList;
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                sethlansUserList = userRepository.findAll();
            } else if (requestingUser.getRoles().contains(Role.ADMINISTRATOR) && !requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                sethlansUserList = userRepository.findAllByRolesNotContaining(Role.SUPER_ADMINISTRATOR);
            } else {
                sethlansUserList = new ArrayList<>();
                sethlansUserList.add(requestingUser);
            }
            var userQueryList = new ArrayList<UserQuery>();

            for (SethlansUser sethlansUser : sethlansUserList) {
                userQueryList.add(userToUserQuery.convert(sethlansUser));
            }
            return userQueryList;

        }
        return null;
    }

    @PostMapping("/create_user")
    public ResponseEntity<Void> createUser(@RequestBody SethlansUser sethlansUser) {
        if (sethlansUser.getPassword().isEmpty() || sethlansUser.getEmail().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findUserByUsername(sethlansUser.getUsername().toLowerCase()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!EmailValidator.getInstance().isValid(sethlansUser.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (sethlansUser.getChallengeList() == null || sethlansUser.getChallengeList().isEmpty()) {
            new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        sethlansUser.setUsername(sethlansUser.getUsername().toLowerCase());
        sethlansUser.setUserID(UUID.randomUUID().toString());
        sethlansUser.setRoles(Stream.of(Role.USER).collect(Collectors.toSet()));
        sethlansUser.setActive(true);
        sethlansUser.setAccountNonExpired(true);
        sethlansUser.setAccountNonLocked(true);
        sethlansUser.setCredentialsNonExpired(true);
        sethlansUser.setPassword(bCryptPasswordEncoder.encode(sethlansUser.getPassword()));
        for (UserChallenge challenge : sethlansUser.getChallengeList()) {
            challenge.setResponse(bCryptPasswordEncoder.encode(challenge.getResponse()));
        }
        userRepository.save(sethlansUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/update_user")
    public ResponseEntity<Void> updateUser(@RequestBody SethlansUser sethlansUser) {
        if (userRepository.findUserByUsername(sethlansUser.getUsername().toLowerCase()).isPresent()) {
            var userInDatabase = userRepository.findUserByUsername(sethlansUser.getUsername().toLowerCase()).get();
            userRepository.save(UserUtils.updateDatabaseUser(sethlansUser, userInDatabase, bCryptPasswordEncoder));
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }


    @GetMapping("/system_id")
    public String getSystemID(@RequestParam String apiKey) {
        if (apiKey.equals(ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY))) {
            return ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID);
        }
        return null;
    }

    @GetMapping("/restart")
    public ResponseEntity<Void> restartSethlans() {
        sethlansManagerService.restart();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/shutdown")
    public ResponseEntity<Void> shutdownSethlans() {
        sethlansManagerService.shutdown();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
