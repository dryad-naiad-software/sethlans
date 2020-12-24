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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.devices.ScanGPU;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * File created by Mario Estrella on 4/22/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class PropertiesUtilsTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @BeforeEach
    void setUp() throws Exception {
        log.info("Initiating Test Setup");
        ConfigUtils.getConfigFile();
        ConfigUtils.writeProperty(ConfigKeys.MODE, SethlansMode.SERVER.toString());
        ConfigUtils.writeProperty(ConfigKeys.CPU_CORES, "3");
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }


    @Test
    void getMode() {
        assertEquals(SethlansMode.SERVER, PropertiesUtils.getMode(), "Values do not match");
    }


    @Test
    void isFirstTime() {
        assertThat(PropertiesUtils.isFirstTime()).isTrue();
    }

    @Test
    void writeMailSettings() throws Exception {
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
        assertThat(ConfigUtils.getProperty(ConfigKeys.MAIL_SERVER_CONFIGURED)).isEqualTo("true");
        assertThat(ConfigUtils.getProperty(ConfigKeys.MAIL_HOST)).isEqualTo("localhost");
        assertThat(ConfigUtils.getProperty(ConfigKeys.MAIL_PORT)).isEqualTo("25");
        assertThat(ConfigUtils.getProperty(ConfigKeys.MAIL_USE_AUTH)).isEqualTo("true");
        assertThat(ConfigUtils.getProperty(ConfigKeys.MAIL_USER)).isEqualTo("test_username@email.local");
        assertThat(ConfigUtils.getEncryptedProperty(ConfigKeys.MAIL_PASS)).isEqualTo("litter");
    }


    @Test
    @Disabled
    void writeNodeSettingsGPU() throws Exception {
        var nodeSettings = NodeSettings.builder()
                .nodeType(NodeType.GPU)
                .selectedGPUs(ScanGPU.listDevices())
                .tileSizeCPU(32)
                .tileSizeGPU(256)
                .gpuCombined(true)
                .build();
        PropertiesUtils.writeNodeSettings(nodeSettings);
        List<GPU> selectedGPUs = PropertiesUtils.getSelectedGPUs();
        assertThat(selectedGPUs.size()).isGreaterThan(0);
        assertThat(ConfigUtils.getProperty(ConfigKeys.NODE_TYPE)).isEqualTo(NodeType.GPU.toString());
        assertThat(ConfigUtils.getProperty(ConfigKeys.TILE_SIZE_CPU)).isEqualTo("0");
        assertThat(ConfigUtils.getProperty(ConfigKeys.TILE_SIZE_GPU)).isEqualTo("256");
        assertThat(ConfigUtils.getProperty(ConfigKeys.CPU_CORES)).isEqualTo("0");
        assertThat(ConfigUtils.getProperty(ConfigKeys.COMBINE_GPU)).isEqualTo("true");
    }

    @Test
    void writeNodeSettingsCPU() throws Exception {
        var nodeSettings = NodeSettings.builder()
                .cores(4)
                .nodeType(NodeType.CPU)
                .selectedGPUs(new ArrayList<>())
                .tileSizeCPU(32)
                .build();
        PropertiesUtils.writeNodeSettings(nodeSettings);
        assertThat(ConfigUtils.getProperty(ConfigKeys.NODE_TYPE)).isEqualTo(NodeType.CPU.toString());
        assertThat(ConfigUtils.getProperty(ConfigKeys.TILE_SIZE_CPU)).isEqualTo("32");
        assertThat(ConfigUtils.getProperty(ConfigKeys.TILE_SIZE_GPU)).isEqualTo("0");
        assertThat(ConfigUtils.getProperty(ConfigKeys.CPU_CORES)).isEqualTo("4");
    }

    @Test
    void writeSetupSettings() throws Exception {
        var setupSettings = SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress("10.10.10.10")
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.DUAL)
                .port("7443").build();
        PropertiesUtils.writeSetupSettings(setupSettings);
        assertThat(ConfigUtils.getProperty(ConfigKeys.MODE)).isEqualTo("DUAL");
        assertThat(ConfigUtils.getProperty(ConfigKeys.LOG_LEVEL)).isEqualTo("DEBUG");
        assertThat(ConfigUtils.getProperty(ConfigKeys.SETHLANS_IP)).isEqualTo("10.10.10.10");
        assertThat(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT)).isEqualTo("7443");
        assertThat(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID)).isNotNull();
        assertThat(ConfigUtils.getProperty(ConfigKeys.SETHLANS_URL)).isEqualTo("https://localhost:7443");
        assertThat(ConfigUtils.getProperty(ConfigKeys.GETTING_STARTED)).isEqualTo("true");
    }


    @Test
    void writeDirectoriesServer() throws Exception {
        String rootDirectory = SETHLANS_DIRECTORY.toString();
        String scriptsDirectory = rootDirectory + File.separator + "scripts";
        String downloadDirectory = rootDirectory + File.separator + "downloads";
        String tempDirectory = rootDirectory + File.separator + "temp";
        String binDirectory = rootDirectory + File.separator + "bin";
        String projectDirectory = rootDirectory + File.separator + "projects";
        String logDirectory = rootDirectory + File.separator + "logs";
        PropertiesUtils.writeDirectories(SethlansMode.SERVER);
        assertThat(ConfigUtils.getProperty(ConfigKeys.ROOT_DIR)).isEqualTo(rootDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.SCRIPTS_DIR)).isEqualTo(scriptsDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR)).isEqualTo(tempDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.BINARY_DIR)).isEqualTo(binDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.LOGGING_DIR)).isEqualTo(logDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.PROJECT_DIR)).isEqualTo(projectDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.DOWNLOAD_DIR)).isEqualTo(downloadDirectory);
        assertThat(new File(rootDirectory)).exists();
        assertThat(new File(scriptsDirectory)).exists();
        assertThat(new File(tempDirectory)).exists();
        assertThat(new File(binDirectory)).exists();
        assertThat(new File(projectDirectory)).exists();
        assertThat(new File(downloadDirectory)).exists();
    }

    @Test
    void writeDirectoriesNode() throws Exception {
        String rootDirectory = SETHLANS_DIRECTORY.toString();
        String scriptsDirectory = rootDirectory + File.separator + "scripts";
        String tempDirectory = rootDirectory + File.separator + "temp";
        String binDirectory = rootDirectory + File.separator + "bin";
        String cacheDirectory = rootDirectory + File.separator + "render_cache";
        String blendFileCacheDirectory = rootDirectory + File.separator + "blendfile_cache";
        String logDirectory = rootDirectory + File.separator + "logs";
        String benchmarkDirectory = rootDirectory + File.separator + "benchmarks";
        PropertiesUtils.writeDirectories(SethlansMode.NODE);
        assertThat(ConfigUtils.getProperty(ConfigKeys.ROOT_DIR)).isEqualTo(rootDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.SCRIPTS_DIR)).isEqualTo(scriptsDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR)).isEqualTo(tempDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.BINARY_DIR)).isEqualTo(binDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.LOGGING_DIR)).isEqualTo(logDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.BENCHMARK_DIR)).isEqualTo(benchmarkDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.CACHE_DIR)).isEqualTo(cacheDirectory);
        assertThat(ConfigUtils.getProperty(ConfigKeys.BLEND_FILE_CACHE_DIR)).isEqualTo(blendFileCacheDirectory);
        assertThat(new File(rootDirectory)).exists();
        assertThat(new File(scriptsDirectory)).exists();
        assertThat(new File(tempDirectory)).exists();
        assertThat(new File(binDirectory)).exists();
        assertThat(new File(benchmarkDirectory)).exists();
        assertThat(new File(cacheDirectory)).exists();
        assertThat(new File(blendFileCacheDirectory)).exists();
    }

    @Test
    void isNodeDisabled() {
        assertThat(PropertiesUtils.isNodeDisabled()).isTrue();
    }
}
