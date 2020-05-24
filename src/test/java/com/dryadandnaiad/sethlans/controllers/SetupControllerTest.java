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

import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * File created by Mario Estrella on 5/24/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("SETUP")
@WebMvcTest(SetupController.class)
class SetupControllerTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }

    @Test
    void saveForm() throws Exception {
        SetupForm form = getSetupForm();
        ObjectMapper objectMapper = new ObjectMapper();
        String formJson = objectMapper.writeValueAsString(form);

        mockMvc.perform(
                post("/api/v1/setup/submit")
                        .contentType(MediaType.APPLICATION_JSON).content(formJson))
                .andExpect(status().isCreated());
    }

    SetupForm getSetupForm() {
        return SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress("10.10.10.10")
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.SERVER)
                .port("7443")
                .user(getUser())
                .mailSettings(getMailSettings()).build();
    }

    User getUser() {
        return User.builder()
                .username("jack1234")
                .password("abcdefg")
                .active(true)
                .email("test@example.com")
                .roles(Stream.of(Role.USER).collect(Collectors.toSet()))
                .tokens(Stream.of("abcd", "efgh").collect(Collectors.toList()))
                .challengeList(new ArrayList<>())
                .build();
    }

    MailSettings getMailSettings() {
        return MailSettings.builder()
                .mailEnabled(true)
                .mailHost("localhost")
                .mailPort("25")
                .replyToAddress("noreply@test.com")
                .smtpAuth(false).build();
    }

}
