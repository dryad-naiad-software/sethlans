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

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.bool.BooleanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Mario Estrella on 4/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/v1/users/")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("list")
    Flux<User> list() {
        return userRepository.findAll();
    }

    @GetMapping("{id}")
    Mono<User> getById(@PathVariable String id) {
        return userRepository.findById(id);
    }

    @GetMapping("{username}")
    Mono<User> getByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username);
    }

    @GetMapping("is_admin/{username}")
    Mono<Boolean> isAdministrator(@PathVariable String username) {
        return BooleanUtils.or(userRepository.existsUserByUsernameAndRolesContains(username, Role.ADMINISTRATOR), userRepository.existsUserByUsernameAndRolesContains(username, Role.SUPER_ADMINISTRATOR));
    }

    @GetMapping("is_super_admin/{username}")
    Mono<Boolean> isSuperAdministrator(@PathVariable String username) {
        return userRepository.existsUserByUsernameAndRolesContains(username, Role.SUPER_ADMINISTRATOR);
    }
}
