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

import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.testutils.TestUtils;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.PythonUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * File created by Mario Estrella on 12/29/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("DUAL")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:sethlans.properties")
@AutoConfigureMockMvc
@DirtiesContext
@Transactional
class ProjectActionsControllerTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Autowired
    private MockMvc mvc;

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

        var scriptDir = SETHLANS_DIRECTORY + File.separator + "scripts";
        new File(scriptDir).mkdirs();
        var binaryDir = SETHLANS_DIRECTORY + File.separator + "binaries";
        new File(binaryDir).mkdirs();
        var pythonDir = binaryDir + File.separator + "python";
        ConfigUtils.writeProperty(ConfigKeys.PYTHON_DIR, pythonDir);
        PythonUtils.copyPythonArchiveToDisk(binaryDir, QueryUtils.getOS());
        PythonUtils.copyAndExtractScripts(scriptDir);
        PythonUtils.installPython(binaryDir, QueryUtils.getOS());

    }

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }

    @Test
    @WithMockUser(username = "testuser1", password = "test1234$", roles = "USER")
    void deleteAllProjects() throws Exception {
        var project1 = TestUtils.getProject();
        var project2 = TestUtils.getProject();
        var project3 = TestUtils.getProject();
        var project4 = TestUtils.getProject();
        var user1 = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()), "testuser1", "test1234$");
        userRepository.save(user1);
        project1.setUser(user1);
        project2.setUser(user1);
        project3.setUser(user1);
        project4.setUser(user1);
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        projectRepository.save(project4);
        assertThat(projectRepository.count()).isEqualTo(4);
        mvc.perform(delete("/api/v1/project/delete_all_projects"))
                .andExpect(status().isOk());
        assertThat(projectRepository.count()).isEqualTo(0);
        projectRepository.deleteAll();


    }


    @Test
    void newProjectUploadFail() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("project_file", "filename.txt", "text/plain", "some xml".getBytes());
        mvc.perform(multipart("/api/v1/project/upload_project_file")
                .file(firstFile))
                .andExpect(status().isBadRequest());

    }


}
