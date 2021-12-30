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
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.services.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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

    public AdminController(SethlansManagerService sethlansManagerService, UserRepository userRepository) {
        this.sethlansManagerService = sethlansManagerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/get_current_user")
    public UserQuery getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
            var converter = new UserToUserQuery();
            return converter.convert(userRepository.findUserByUsername(auth.getName()).get());
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
            var converter = new UserToUserQuery();

            for (User user:userList) {
                userQueryList.add(converter.convert(user));
            }
            return userQueryList;

        }
        return null;
    }

    @PostMapping("/create_user")
    public void createUser(){

    }

    @PostMapping("/update_user")
    public void updateUser(){

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
