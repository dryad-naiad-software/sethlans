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

import com.dryadandnaiad.sethlans.devices.ScanGPU;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
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
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * File created by Mario Estrella on 7/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("DUAL")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:sethlans.properties")
@DirtiesContext
class BenchmarkServiceTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Resource
    BlenderArchiveRepository blenderArchiveRepository;

    @Resource
    ServerRepository serverRepository;

    @Resource
    RenderTaskRepository renderTaskRepository;

    @Resource
    NodeRepository nodeRepository;

    @Autowired
    DownloadService downloadService;

    @Autowired
    BenchmarkService benchmarkService;

    @BeforeAll
    static void beforeAll() throws Exception {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
        var setupSettings = SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress(QueryUtils.getIP())
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.DUAL)
                .port("7443").build();
        PropertiesUtils.writeSetupSettings(setupSettings);
        PropertiesUtils.writeDirectories(SethlansMode.DUAL);
        var mailSettings = MailSettings.builder()
                .mailEnabled(true)
                .mailHost("localhost")
                .mailPort("25")
                .replyToAddress("noreply@test.com")
                .smtpAuth(true)
                .username("test_username@email.local")
                .password("litter")
                .build();
        PropertiesUtils.writeMailSettings(mailSettings);

    }

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }


    @Test
    void cpuBenchmarkTest() throws Exception {
        var nodeSettings = NodeSettings.builder().nodeType(NodeType.CPU).tileSizeCPU(32).cores(8).build();
        PropertiesUtils.writeNodeSettings(nodeSettings);

        blenderArchiveRepository.save(BlenderArchive.builder()
                .blenderOS(QueryUtils.getOS())
                .downloaded(false)
                .blenderVersion("2.79b")
                .build());

        var blenderBinary = blenderArchiveRepository.findAll().get(0);
        assertThat(blenderBinary).isNotNull();
        Thread.sleep(10000);
        while (!blenderBinary.isDownloaded()) {
            blenderBinary = blenderArchiveRepository.findAll().get(0);
            Thread.sleep(1000);
        }
        assertThat(new File(SETHLANS_DIRECTORY + File.separator + "downloads")).isNotEmptyDirectory();
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


        benchmarkService.processBenchmarkRequest(server, blenderBinary);
        while (renderTaskRepository.findAll().size() == 0) {
            Thread.sleep(10000);
        }

        var renderTask = renderTaskRepository.findAll().get(0);
        assertThat(renderTask).isNotNull();
        Thread.sleep(10000);
        while (!renderTask.isComplete()) {
            renderTask = renderTaskRepository.findAll().get(0);
            Thread.sleep(1000);
        }
        var cpuRating = PropertiesUtils.getCPURating();
        assertThat(cpuRating > 0);
        renderTaskRepository.deleteAll();

    }

    @Test
    @Disabled
    void gpuBenchmarkTest() throws Exception {
        var selectedGPUs = ScanGPU.listDevices();
        var nodeSettings = NodeSettings.builder().nodeType(NodeType.CPU_GPU).tileSizeCPU(32)
                .tileSizeGPU(256).cores(8).selectedGPUs(selectedGPUs).build();
        PropertiesUtils.writeNodeSettings(nodeSettings);

        blenderArchiveRepository.save(BlenderArchive.builder()
                .blenderOS(QueryUtils.getOS())
                .downloaded(false)
                .blenderVersion("2.83")
                .build());

        var blenderBinary = blenderArchiveRepository.findAll().get(0);
        assertThat(blenderBinary).isNotNull();
        Thread.sleep(10000);
        while (!blenderBinary.isDownloaded()) {
            blenderBinary = blenderArchiveRepository.findAll().get(0);
            Thread.sleep(1000);
        }
        assertThat(new File(SETHLANS_DIRECTORY + File.separator + "downloads")).isNotEmptyDirectory();
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


        benchmarkService.processBenchmarkRequest(server, blenderBinary);
        while (renderTaskRepository.findAll().size() == 0) {
            Thread.sleep(10000);
        }
        var itemsBenchmarked = selectedGPUs.size() + 1;


        var renderTask = renderTaskRepository.findAll().get(itemsBenchmarked - 1);
        assertThat(renderTask).isNotNull();
        Thread.sleep(10000);
        while (!renderTask.isComplete()) {
            renderTask = renderTaskRepository.findAll().get(itemsBenchmarked - 1);
            Thread.sleep(1000);
        }
        var cpuRating = PropertiesUtils.getCPURating();
        assertThat(cpuRating > 0);
        renderTaskRepository.deleteAll();

    }

    @Test
    void benchmarkStatus() {
        var server = Server.builder().systemID(UUID.randomUUID().toString()).build();
        var serverInfo = TaskServerInfo.builder().systemID(server.getSystemID()).build();
        var renderTask = RenderTask.builder().serverInfo(serverInfo).benchmark(true).complete(true).build();
        var renderTask2 = RenderTask.builder().serverInfo(serverInfo).benchmark(false).complete(true).build();
        renderTaskRepository.save(renderTask);
        renderTaskRepository.save(renderTask2);
        assertTrue(benchmarkService.benchmarkStatus(server));
        var renderTask3 = RenderTask.builder().serverInfo(serverInfo).benchmark(false).complete(true).build();
        var renderTask4 = RenderTask.builder().serverInfo(serverInfo).benchmark(true).complete(false).build();
        renderTaskRepository.save(renderTask3);
        renderTaskRepository.save(renderTask4);
        assertFalse(benchmarkService.benchmarkStatus(server));
        renderTaskRepository.deleteAll();
    }
}
