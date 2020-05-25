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

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @BeforeEach
    void setUp() throws Exception {
        log.info("Initiating Test Setup");
        ConfigUtils.getConfigFile();
        ConfigUtils.writeProperty(ConfigKeys.MODE, SethlansMode.SERVER.toString());
        ConfigUtils.writeProperty(ConfigKeys.CPU_CORES, "3");
    }

    @AfterEach
    void tearDown() {
        ConfigUtils.getConfigFile().delete();
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
    void writeMailSettings() {
    }

    @Test
    void getPort() {
    }

    @Test
    void writeNodeSettings() {
    }
}
