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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.testutils.TestUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * File created by Mario Estrella on 12/30/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("DUAL")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:sethlans.properties")
@DirtiesContext
@Transactional
class ProjectServiceTest {
    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");


    @Resource
    ProjectRepository projectRepository;

    @Resource
    UserRepository userRepository;

    @Resource
    ProjectService projectService;

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
    void deleteProject() {
    }

    @Test
    @WithMockUser(username = "testuser1", password = "test1234$", roles = "USER")
    void deleteAllProjectsByUserAsUser() {
        var project1 = TestUtils.getProject();
        var project2 = TestUtils.getProject();
        var project3 = TestUtils.getProject();
        var project4 = TestUtils.getProject();
        var project5 = TestUtils.getProject();
        var user1 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser1", "test1234$");
        var user2 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser2", "test123456%");
        var adminUser = TestUtils.getUser(Stream.of(Role.ADMINISTRATOR).collect(Collectors.toSet()), "administrator1", "test1234$");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(adminUser);
        project1.setUser(user1);
        project2.setUser(user1);
        project3.setUser(user2);
        project4.setUser(user1);
        project5.setUser(adminUser);
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectRepository.save(project5);
        assertThat(projectRepository.count()).isEqualTo(5);
        projectService.deleteAllProjectsByUser(user2.getUserID());
        assertThat(projectRepository.count()).isEqualTo(5);
        projectService.deleteAllProjectsByUser(user1.getUserID());
        assertThat(projectRepository.count()).isEqualTo(2);
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "administrator1", password = "test1234$", roles = "ADMINISTRATOR")
    void deleteAllProjectsByUserAsAdmin() {
        var project1 = TestUtils.getProject();
        var project2 = TestUtils.getProject();
        var project3 = TestUtils.getProject();
        var project4 = TestUtils.getProject();
        var project5 = TestUtils.getProject();
        var user1 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser1", "test1234$");
        var user2 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser2", "test123456%");
        var adminUser = TestUtils.getUser(Stream.of(Role.ADMINISTRATOR).collect(Collectors.toSet()), "administrator1", "test1234$");
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(adminUser);
        project1.setUser(user1);
        project2.setUser(user1);
        project3.setUser(user2);
        project4.setUser(user1);
        project5.setUser(adminUser);
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectRepository.save(project5);
        assertThat(projectRepository.count()).isEqualTo(5);
        projectService.deleteAllProjectsByUser(user2.getUserID());
        assertThat(projectRepository.count()).isEqualTo(4);
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testuser1", password = "test1234$", roles = "USER")
    void deleteAllProjectsAsAUser() throws Exception {
        var project1 = TestUtils.getProject();
        var project2 = TestUtils.getProject();
        var project3 = TestUtils.getProject();
        var project4 = TestUtils.getProject();
        var user1 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser1", "test1234$");
        var user2 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser2", "test123456%");
        userRepository.save(user1);
        userRepository.save(user2);
        project1.setUser(user1);
        project2.setUser(user1);
        project3.setUser(user2);
        project4.setUser(user1);
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectService.deleteAllProjects();
        assertThat(projectRepository.count()).isEqualTo(1);
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "testuser1", password = "test1234$", roles = "ADMINISTRATOR")
    void deleteAllProjectsAsAnAdmin() throws Exception {
        var project1 = TestUtils.getProject();
        var project2 = TestUtils.getProject();
        var project3 = TestUtils.getProject();
        var project4 = TestUtils.getProject();
        var user1 = TestUtils.getUser(Stream.of(Role.ADMINISTRATOR).collect(Collectors.toSet()), "testuser1", "test1234$");
        var user2 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser2", "test123456%");
        userRepository.save(user1);
        userRepository.save(user2);
        project1.setUser(user2);
        project2.setUser(user2);
        project3.setUser(user2);
        project4.setUser(user2);
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        projectService.deleteAllProjects();
        assertThat(projectRepository.count()).isEqualTo(0);
        projectRepository.deleteAll();
    }
}
