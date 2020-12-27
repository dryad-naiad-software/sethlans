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
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectView;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.testutils.TestUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("DUAL")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:sethlans.properties")
@AutoConfigureMockMvc
@DirtiesContext
class ProjectUIControllerTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Autowired
    private MockMvc mvc;

    @Resource
    NodeRepository nodeRepository;

    @Resource
    ProjectRepository projectRepository;

    @Resource
    UserRepository userRepository;


    @BeforeAll
    static void beforeAll() throws Exception {
        var setupSettings = SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress(QueryUtils.getIP())
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.DUAL)
                .port("7443").build();
        var nodeSettings = NodeSettings.builder().nodeType(NodeType.CPU).tileSizeCPU(32).cores(4).build();
        PropertiesUtils.writeNodeSettings(nodeSettings);
        PropertiesUtils.writeSetupSettings(setupSettings);
        PropertiesUtils.writeDirectories(SethlansMode.DUAL);
        var mailSettings = MailSettings.builder()
                .mailEnabled(false)
                .build();
        PropertiesUtils.writeMailSettings(mailSettings);

    }

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }

    @Test
    void nodesReady() throws Exception {

        var node1 = Node.builder()
                .ipAddress("10.10.10.12")
                .networkPort("7443")
                .nodeType(NodeType.CPU)
                .systemID(UUID.randomUUID().toString())
                .active(false)
                .build();
        nodeRepository.save(node1);

        var result = mvc.perform(get("/api/v1/project/nodes_ready")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("false");

        var nodeList = new ArrayList<Node>();
        nodeList.add(Node.builder()
                .ipAddress("10.10.10.10")
                .networkPort("7443")
                .active(true)
                .nodeType(NodeType.CPU)
                .systemID(UUID.randomUUID().toString())
                .build());
        nodeList.add(Node.builder()
                .ipAddress("10.10.10.1")
                .networkPort("7443")
                .nodeType(NodeType.CPU)
                .systemID(UUID.randomUUID().toString())
                .active(true)
                .build());
        for (Node node : nodeList) {
            nodeRepository.save(node);
        }

        var result2 = mvc.perform(get("/api/v1/project/nodes_ready")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andReturn();
        assertThat(result2.getResponse().getContentAsString()).isEqualTo("true");

        var node2 = Node.builder()
                .ipAddress("10.10.10.12")
                .networkPort("7443")
                .nodeType(NodeType.CPU)
                .systemID(UUID.randomUUID().toString())
                .active(true)
                .build();
        nodeRepository.save(node2);

        var result3 = mvc.perform(get("/api/v1/project/nodes_ready")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andReturn();
        assertThat(result3.getResponse().getContentAsString()).isEqualTo("true");


    }

    @Test
    @WithMockUser(username = "testuser", password = "test1234", roles = "USER")
    void getProjectsWithUser() throws Exception {
        var roles = new HashSet<Role>();
        roles.add(Role.USER);
        userRepository.save(TestUtils.getUser(roles, "testuser", "test1234"));
        userRepository.save(TestUtils.getUser(roles, "anotherUser", "aSimplePass2"));

        var user = userRepository.findUserByUsername("testuser").get();
        var anotherUser = userRepository.findUserByUsername("anotherUser").get();

        roles.remove(Role.USER);
        roles.add(Role.SUPER_ADMINISTRATOR);

        userRepository.save(TestUtils.getUser(roles, "adminUser", "test1234"));

        var adminUser = userRepository.findUserByUsername("adminUser").get();

        var objectMapper = new ObjectMapper();

        for (int i = 0; i < 5; i++) {
            var project = TestUtils.getProject();
            project.setUser(user);
            projectRepository.save(project);
        }

        for (int i = 0; i < 5; i++) {
            var project = TestUtils.getProject();
            project.setUser(anotherUser);
            projectRepository.save(project);
        }

        var project6 = TestUtils.getProject();
        project6.setUser(adminUser);
        projectRepository.save(project6);

        assertThat(projectRepository.findAll().size()).isEqualTo(11);

        var result = mvc.perform(get("/api/v1/project/project_list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        var projectViewList = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ProjectView>>() {
                });
        assertThat(projectViewList.size()).isEqualTo(5);
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "adminUser", password = "test1234", roles = "SUPER_ADMINISTRATOR")
    void getProjectsWithAdmin() throws Exception {
        var roles = new HashSet<Role>();
        roles.add(Role.USER);
        userRepository.save(TestUtils.getUser(roles, "testuser", "test1234"));
        userRepository.save(TestUtils.getUser(roles, "anotherUser", "aSimplePass2"));

        var user = userRepository.findUserByUsername("testuser").get();
        var anotherUser = userRepository.findUserByUsername("anotherUser").get();

        roles.remove(Role.USER);
        roles.add(Role.SUPER_ADMINISTRATOR);

        userRepository.save(TestUtils.getUser(roles, "adminUser", "test1234"));

        var adminUser = userRepository.findUserByUsername("adminUser").get();

        var objectMapper = new ObjectMapper();

        for (int i = 0; i < 5; i++) {
            var project = TestUtils.getProject();
            project.setUser(user);
            projectRepository.save(project);
        }

        for (int i = 0; i < 5; i++) {
            var project = TestUtils.getProject();
            project.setUser(anotherUser);
            projectRepository.save(project);
        }

        var project6 = TestUtils.getProject();
        project6.setUser(adminUser);
        projectRepository.save(project6);

        assertThat(projectRepository.findAll().size()).isEqualTo(11);

        var result = mvc.perform(get("/api/v1/project/project_list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        var projectViewList = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<ProjectView>>() {
                });
        assertThat(projectViewList.size()).isEqualTo(11);
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

}
