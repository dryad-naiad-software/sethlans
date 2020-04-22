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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Created by Mario Estrella on 4/20/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class ConfigUtilsTest {

    private final String value = UUID.randomUUID().toString();
    private final ConfigKeys key = ConfigKeys.ACCESS_KEY;

    @BeforeEach
    void setUp() {
        log.info("Initiating Test Setup");
        ConfigUtils.getConfigFile();
        ConfigUtils.writeProperty(key, value);
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
    void writeProperty() throws IOException {
        var configFile = ConfigUtils.getConfigFile();
        ConfigUtils.writeProperty(ConfigKeys.MAIL_HOST, "localhost.local");
        assertTrue("Key is not present", FileUtils.readFileToString(configFile,
                "UTF-8").contains(ConfigKeys.MAIL_HOST.toString()));
        assertTrue("Value is not present", FileUtils.readFileToString(configFile,
                "UTF-8").contains("localhost.local"));
    }

    @Test
    void getProperty() {
        assertTrue("Incorrect value retrieved", ConfigUtils.getProperty(key).contains(value));
        assertNotNull("Unable to find value in sethlans.properties located in resources",
                ConfigUtils.getProperty(ConfigKeys.LOG_LEVEL));
        assertNull("Property should be null", ConfigUtils.getProperty(ConfigKeys.BENCHMARK_DIR));
    }
}
