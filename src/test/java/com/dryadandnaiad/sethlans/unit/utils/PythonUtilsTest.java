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

import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.utils.PythonUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 4/30/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class PythonUtilsTest {

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
    void copyPythonToDisk() {
        var result = PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.LINUX_64);
        assertThat(result).isTrue();
        result = PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.UNSUPPORTED);
        assertThat(result).isFalse();
        result = PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.WINDOWS_32);
        assertThat(result).isFalse();
    }

    @Test
    void copyAndExtractScripts() {
        assertThat(PythonUtils.copyAndExtractScripts(TEST_DIRECTORY.toString())).isTrue();
    }

    @Test
    void installPythonWindows() {
        assertThat(PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.WINDOWS_64)).isTrue();
        assertThat(PythonUtils.installPython(TEST_DIRECTORY.toString(), OS.WINDOWS_64)).isTrue();
    }

    @Test
    void installPythonLinux() {
        assertThat(PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.LINUX_64)).isTrue();
        assertThat(PythonUtils.installPython(TEST_DIRECTORY.toString(), OS.LINUX_64)).isTrue();
    }

    @Test
    void installPythonMac() {
        assertThat(PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.MACOS)).isTrue();
        assertThat(PythonUtils.installPython(TEST_DIRECTORY.toString(), OS.MACOS)).isTrue();
    }

    @Test
    void installPythonUnsupported() {
        assertThat(PythonUtils.copyPythonArchiveToDisk(TEST_DIRECTORY.toString(), OS.WINDOWS_32)).isFalse();
        assertThat(PythonUtils.installPython(TEST_DIRECTORY.toString(), OS.WINDOWS_32)).isFalse();
    }

}
