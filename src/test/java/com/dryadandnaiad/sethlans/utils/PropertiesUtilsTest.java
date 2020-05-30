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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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
    void getSelectedCores() throws Exception {
        assertEquals("Values do not match", "3", PropertiesUtils.getSelectedCores());
        ConfigUtils.writeProperty(ConfigKeys.CPU_CORES, "2");
        assertEquals("Values do not match", "2", PropertiesUtils.getSelectedCores());
    }

    @Test
    void getMode() {
        assertEquals("Values do not match", SethlansMode.SERVER, PropertiesUtils.getMode());
    }

    @Test
    void getIP() {
        val ipAddress = PropertiesUtils.getIP();
        val validator = InetAddressValidator.getInstance();
        assertNotNull(ipAddress);
        assertThat(ipAddress).isNotEqualTo("0.0.0.0");
        assertThat(ipAddress).isNotEqualTo("255.255.255.255");
        assertThat(ipAddress).isNotEqualTo("127.0.0.1");
        assertTrue("Not a valid IP address", validator.isValidInet4Address(ipAddress));
    }

    @Test
    void getFirstTime() {
        assertThat(PropertiesUtils.getFirstTime()).isTrue();
    }

    @Test
    void getPort() {
        assertThat(PropertiesUtils.getPort().equals("7443"));
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
        ObjectMapper objectMapper = new ObjectMapper();
        List<GPU> selectedGPUs = objectMapper.readValue(ConfigUtils.getProperty(ConfigKeys.SELECTED_GPU),
                new TypeReference<>() {
                });
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
    }

    @Test
    void writeServerSettings() {
    }

    @Test
    void writeDirectories() {
    }
}
