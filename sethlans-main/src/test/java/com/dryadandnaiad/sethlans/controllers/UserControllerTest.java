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

import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by Mario Estrella on 4/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class UserControllerTest {
    WebTestClient webTestClient;
    UserRepository userRepository;
    UserController userController;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userController = new UserController(userRepository);
        webTestClient = WebTestClient.bindToController(userController).build();

    }

    @Test
    void list() {
        BDDMockito.given(userRepository.findAll())
                .willReturn(Flux.just(User.builder()
                                .username("NewUser1")
                                .build(),
                        User.builder()
                                .username("NewUser2")
                                .build()));

        webTestClient.get()
                .uri("/api/v1/users/list")
                .exchange()
                .expectBodyList(User.class)
                .hasSize(2);
    }

    @Test
    void getById() {
        BDDMockito.given(userRepository.findById("someid"))
                .willReturn(Mono.just(User.builder()
                        .username("Bob")
                        .build()));

        webTestClient.get()
                .uri("/api/v1/users/someid")
                .exchange()
                .expectBody(User.class);
    }

    @Test
    void isAdministrator() {
    }

    @Test
    void isSuperAdministrator() {
    }

    @Test
    void getByUsername() {
    }
}
