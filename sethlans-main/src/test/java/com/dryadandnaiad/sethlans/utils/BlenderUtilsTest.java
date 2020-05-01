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

import com.dryadandnaiad.sethlans.enums.OS;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.springframework.util.FileSystemUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.OS.MAC;

/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class BlenderUtilsTest {

    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");

    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();

    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @Test
    void parseBlenderFile() {
    }

    @Test
    void downloadBlender() {
        assertThat(BlenderUtils
                .downloadBlender("2.80b",
                        "resource",
                        TEST_DIRECTORY.toString(),
                        OS.LINUX_64)).isNull();
        var blenderDownload = BlenderUtils
                .downloadBlender("2.79b",
                        "resource",
                        TEST_DIRECTORY.toString(),
                        OS.LINUX_64);
        assertThat(blenderDownload).isNotNull();
        assertThat(blenderDownload).exists();
    }

    @Test
    void availableBlenderVersions() {
        assertThat(BlenderUtils.availableBlenderVersions("resource")).hasSizeGreaterThan(0);
    }

    @Test
    void extractBlenderWindowsBinary() {
        var version = "2.82a";
        var blenderDownload = BlenderUtils
                .downloadBlender(version,
                        "resource",
                        TEST_DIRECTORY.toString(),
                        OS.WINDOWS_64);
        assertThat(BlenderUtils.extractBlender(TEST_DIRECTORY.toString(), OS.WINDOWS_64,
                blenderDownload.toString(), version)).isTrue();
        assertThat(blenderDownload).doesNotExist();
    }

    @Test
    void extractBlenderLinuxBinary() {
        var version = "2.82a";
        var blenderDownload = BlenderUtils
                .downloadBlender(version,
                        "resource",
                        TEST_DIRECTORY.toString(),
                        OS.LINUX_64);
        assertThat(BlenderUtils.extractBlender(TEST_DIRECTORY.toString(), OS.LINUX_64,
                blenderDownload.toString(), version)).isTrue();
        assertThat(blenderDownload).doesNotExist();
    }


    @Test
    @EnabledOnOs(MAC)
    void extractBlenderMacBinary() {
        var version = "2.82a";
        var blenderDownload = BlenderUtils
                .downloadBlender(version,
                        "resource",
                        TEST_DIRECTORY.toString(),
                        OS.MACOS);
        assertThat(BlenderUtils.extractBlender(TEST_DIRECTORY.toString(), OS.MACOS,
                blenderDownload.toString(), version)).isTrue();
        assertThat(blenderDownload).doesNotExist();
    }

}
