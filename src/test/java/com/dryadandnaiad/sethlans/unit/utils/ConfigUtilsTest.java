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

package com.dryadandnaiad.sethlans.unit.utils;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * File created by Mario Estrella on 4/20/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class ConfigUtilsTest {

    @BeforeEach
    void setUp() {
        ConfigUtils.getConfigFile();
    }

    @AfterEach
    void tearDown() {
        ConfigUtils.getConfigFile().delete();
    }

    @Test
    void getConfigFile() {
        assertThat(ConfigUtils.getConfigFile()).exists();
    }

    @Test
    void writeProperty() throws Exception {
        var configFile = ConfigUtils.getConfigFile();
        ConfigUtils.writeProperty(ConfigKeys.MAIL_HOST, "localhost.local");
        assertTrue(FileUtils.readFileToString(configFile,
                "UTF-8").contains(ConfigKeys.MAIL_HOST.toString()), "Key is not present");
        assertTrue(FileUtils.readFileToString(configFile,
                "UTF-8").contains("localhost.local"), "Value is not present");
    }

    @Test
    void getProperty() {
        assertNotNull(ConfigUtils.getProperty(ConfigKeys.LOG_LEVEL), "Unable to find value in sethlans.properties located in resources");
        assertNull(ConfigUtils.getProperty(ConfigKeys.BENCHMARK_DIR), "Property should be null");
    }
}
