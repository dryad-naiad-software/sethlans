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

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskServerInfo;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * File created by Mario Estrella on 6/16/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("DUAL")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:sethlans.properties")
@AutoConfigureMockMvc
@DirtiesContext
class AdminNodeEndPointControllerTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Autowired
    private MockMvc mvc;

    @Resource
    ServerRepository serverRepository;

    @Resource
    NodeRepository nodeRepository;

    @Resource
    RenderTaskRepository renderTaskRepository;

    @Resource
    BlenderArchiveRepository blenderArchiveRepository;

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
    void addServer() throws Exception {
        var server = Server.builder()
                .ipAddress(QueryUtils.getIP())
                .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                .hostname(QueryUtils.getHostname())
                .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                .build();
        var objectMapper = new ObjectMapper();
        var serverJson = objectMapper.writeValueAsString(server);
        mvc.perform(post("/api/v1/management/add_server_to_node")
                .contentType(MediaType.APPLICATION_JSON).content(serverJson))
                .andExpect(status().isCreated());
        var result = mvc.perform(get("/api/v1/management/list_servers_on_node")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        var servers = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Server>>() {
        });
        assertThat(servers).hasSizeGreaterThan(0);
    }

    @Test
    void isNodeDisabled() throws Exception {
        var result = mvc.perform(get("/api/v1/management/node_disabled")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("false");
    }


    @Test
    void requestBenchmark() throws Exception {
        // This test is just checking to make sure the endpoint is active, the actual logic is tested in the benchmark
        // service series of tests
        var nodeSettings = NodeSettings.builder().nodeType(NodeType.CPU).tileSizeCPU(32).cores(8).build();
        PropertiesUtils.writeNodeSettings(nodeSettings);

        var systemInfo = QueryUtils.getCurrentSystemInfo();

        var node = Node.builder()
                .nodeType(PropertiesUtils.getNodeType())
                .os(QueryUtils.getOS())
                .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                .ipAddress(systemInfo.getIpAddress())
                .hostname(systemInfo.getHostname())
                .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                .cpu(systemInfo.getCpu())
                .selectedGPUs(PropertiesUtils.getSelectedGPUs())
                .build();

        nodeRepository.save(node);


        var server = Server.builder()
                .ipAddress(QueryUtils.getIP())
                .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                .hostname(QueryUtils.getHostname())
                .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT)).build();

        serverRepository.save(server);

        var objectMapper = new ObjectMapper();
        var serverJSON = objectMapper.writeValueAsString(server);

        mvc.perform(get("/api/v1/management/benchmark_request")
                .contentType(MediaType.APPLICATION_JSON).content(serverJSON))
                .andExpect(status().isInternalServerError());

        serverRepository.delete(server);

        mvc.perform(get("/api/v1/management/benchmark_request")
                .contentType(MediaType.APPLICATION_JSON).content(serverJSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void benchmarkStatus() throws Exception {
        var objectMapper = new ObjectMapper();
        var server = Server.builder().systemID(UUID.randomUUID().toString()).build();
        var serverJSON = objectMapper.writeValueAsString(server);

        var serverInfo = TaskServerInfo.builder().systemID(server.getSystemID()).build();
        serverRepository.save(server);
        var renderTask = RenderTask.builder().serverInfo(serverInfo).benchmark(true).complete(true).build();
        var renderTask2 = RenderTask.builder().serverInfo(serverInfo).benchmark(false).complete(true).build();
        renderTaskRepository.save(renderTask);
        renderTaskRepository.save(renderTask2);
        var result = mvc.perform(get("/api/v1/management/benchmark_status")
                .contentType(MediaType.APPLICATION_JSON).content(serverJSON))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("true");
        var renderTask3 = RenderTask.builder().serverInfo(serverInfo).benchmark(false).complete(true).build();
        var renderTask4 = RenderTask.builder().serverInfo(serverInfo).benchmark(true).complete(false).build();
        renderTaskRepository.save(renderTask3);
        renderTaskRepository.save(renderTask4);
        result = mvc.perform(get("/api/v1/management/benchmark_status")
                .contentType(MediaType.APPLICATION_JSON).content(serverJSON))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("false");
        renderTaskRepository.deleteAll();
    }
}
