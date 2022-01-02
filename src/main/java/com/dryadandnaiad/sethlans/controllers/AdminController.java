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
import com.dryadandnaiad.sethlans.models.user.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    public UserQuery getUser(@RequestParam String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            var requestingUser = userRepository.findUserByUsername(auth.getName()).get();
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR) || requestingUser.getRoles().contains(Role.ADMINISTRATOR)) {

                if (userRepository.findUserByUsername(username).isPresent()) {
                    return userToUserQuery.convert(userRepository.findUserByUsername(username).get());
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

    @GetMapping("/user_list")
    public List<UserQuery> getUserList() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            var requestingUser = userRepository.findUserByUsername(auth.getName()).get();
            List<User> userList;
            if (requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                userList = userRepository.findAll();
            } else if (requestingUser.getRoles().contains(Role.ADMINISTRATOR) && !requestingUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                userList = userRepository.findAllByRolesNotContaining(Role.SUPER_ADMINISTRATOR);
            } else {
                userList = new ArrayList<>();
                userList.add(requestingUser);
            }
            var userQueryList = new ArrayList<UserQuery>();

            for (User user : userList) {
                userQueryList.add(userToUserQuery.convert(user));
            }
            return userQueryList;

        }
        return null;
    }

    @PostMapping("/create_user")
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        if (user.getPassword().isEmpty() || user.getEmail().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findUserByUsername(user.getUsername().toLowerCase()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if(!EmailValidator.getInstance().isValid(user.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (user.getChallengeList() == null || user.getChallengeList().isEmpty()) {
            new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        user.setUsername(user.getUsername().toLowerCase());
        user.setUserID(UUID.randomUUID().toString());
        user.setRoles(Stream.of(Role.USER).collect(Collectors.toSet()));
        user.setActive(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        for (UserChallenge challenge : user.getChallengeList()) {
            challenge.setResponse(bCryptPasswordEncoder.encode(challenge.getResponse()));
        }
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/update_user")
    public ResponseEntity<Void> updateUser(@RequestBody User user) {
        if (userRepository.findUserByUsername(user.getUsername().toLowerCase()).isPresent()) {
            var userInDatabase = userRepository.findUserByUsername(user.getUsername().toLowerCase()).get();
            userRepository.save(UserUtils.updateDatabaseUser(user, userInDatabase, bCryptPasswordEncoder));
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }


    @GetMapping("/system_id")
    public String getSystemID() {
        return ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID);
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
