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

import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Created by Mario Estrella on 4/19/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class QueryUtilsTest {

    @BeforeEach
    void setUp() {
        log.info("Initiating Test Setup");
        ConfigUtils.getConfigFile();
        ConfigUtils.writeProperty(ConfigKeys.MODE, SethlansMode.SERVER.toString());
    }

    @AfterEach
    void tearDown() {
        ConfigUtils.getConfigFile().delete();
    }

    @Test
    void getShortUUID() {
        val shortUUID = QueryUtils.getShortUUID();
        assertThat(shortUUID).hasSize(13);
        assertThat(shortUUID).contains("-");
    }

    @Test
    void getOS() {
        val os = QueryUtils.getOS();
        assertNotNull(os);
        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(os.getName()).contains("Windows");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(os.getName()).contains("Linux");
        }
        if (SystemUtils.IS_OS_MAC) {
            assertThat(os.getName()).contains("MacOS");
        }

    }

    @Test
    void getMode() {
        assertEquals("Values do not match", SethlansMode.SERVER, QueryUtils.getMode());
    }

    @Test
    void getHostname() {
        val hostname = QueryUtils.getHostname();
        assertNotNull(hostname);
        assertThat(hostname).doesNotContain(".");
        assertThat(hostname).isUpperCase();
    }

    @Test
    void getIP() {
        val ipAddress = QueryUtils.getIP();
        val validator = InetAddressValidator.getInstance();
        assertNotNull(ipAddress);
        assertThat(ipAddress).isNotEqualTo("0.0.0.0");
        assertThat(ipAddress).isNotEqualTo("255.255.255.255");
        assertThat(ipAddress).isNotEqualTo("127.0.0.1");
        assertTrue("Not a valid IP address", validator.isValidInet4Address(ipAddress));
    }

    @Test
    void getFirstTime() {
        assertThat(QueryUtils.getFirstTime()).isTrue();
    }

    @Test
    void getAvailableMethods() {
        val availableMethods = QueryUtils.getAvailableMethods();
        assertNotNull(availableMethods);
        assertThat(availableMethods).hasSizeGreaterThan(0);
        if (availableMethods.size() > 1) {
            assertThat(availableMethods).contains(ComputeOn.GPU);
        }
    }

    @Test
    void getCurrentSystemInfo() {
        val serverSystemInfo = QueryUtils.getCurrentSystemInfo(Server.class);
        val nodeSystemInfo = QueryUtils.getCurrentSystemInfo(Node.class);
        assertThat(serverSystemInfo).isInstanceOf(Server.class);
        assertThat(nodeSystemInfo).isInstanceOf(Node.class);
    }

    @Test
    void getVersion() {
        val version = QueryUtils.getVersion();
        assertNotNull(version);
    }
}
