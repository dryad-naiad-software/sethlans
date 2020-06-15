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

import com.dryadandnaiad.sethlans.devices.ScanGPU;
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.settings.ServerSettings;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * File created by Mario Estrella on 5/24/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("SETUP")
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class SetupControllerTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Autowired
    MockMvc mockMvc;


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }

    @Test
    void completeSetup() throws Exception {
        var form = getSetupForm();
        var objectMapper = new ObjectMapper();
        var formJson = objectMapper.writeValueAsString(form);
        System.out.println(formJson);

        mockMvc.perform(
                post("/api/v1/setup/submit")
                        .contentType(MediaType.APPLICATION_JSON).content(formJson))
                .andExpect(status().isCreated());
    }

    @Test
    void prePopulatedSetupForm() throws Exception {
        var result = mockMvc.perform(get("/api/v1/setup/get_setup")).andExpect(status().isOk()).andReturn();
        var objectMapper = new ObjectMapper();
        var setupForm = objectMapper.readValue(result.getResponse().getContentAsString(), new
                TypeReference<SetupForm>() {
                });
        var availableGPU = ScanGPU.listDevices();
        assertThat(setupForm).isNotNull();
        assertThat(setupForm.getIpAddress()).isEqualTo(QueryUtils.getIP());
        assertThat(setupForm.getAvailableTypes().size()).isGreaterThan(0);
        assertThat(setupForm.getBlenderVersions()).isNotNull();
        assertThat(setupForm.getLogLevel()).isEqualTo(LogLevel.INFO);
        assertThat(setupForm.getAppURL()).isEqualTo("https://" + QueryUtils.getHostname().toLowerCase() + ":" +
                ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT) + "/");
        assertThat(setupForm.getAvailableGPUs()).isEqualTo(availableGPU);
    }


    SetupForm getSetupForm() {
        return SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress(QueryUtils.getIP())
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.DUAL)
                .port("7443")
                .user(getUser())
                .nodeSettings(getNodeSettings())
                .serverSettings(getServerSettings())
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

    NodeSettings getNodeSettings() {
        return NodeSettings.builder()
                .cores(4)
                .nodeType(NodeType.CPU)
                .selectedGPUs(new ArrayList<>())
                .tileSizeCPU(32)
                .build();
    }

    ServerSettings getServerSettings() {
        return ServerSettings.builder()
                .blenderVersion("2.79b").build();
    }


}
