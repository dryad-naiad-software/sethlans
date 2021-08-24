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

package com.dryadandnaiad.sethlans.unit.blender;

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.tools.TestFileUtils;
import com.dryadandnaiad.sethlans.utils.PythonUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
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
@Slf4j
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

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @Test
    void parseBlendFiles() {
        var scriptDir = TEST_DIRECTORY + File.separator + "scripts";
        new File(scriptDir).mkdirs();
        var binaryDir = TEST_DIRECTORY + File.separator + "binaries";
        new File(binaryDir).mkdirs();
        var pythonDir = binaryDir + File.separator + "python";
        PythonUtils.copyPythonArchiveToDisk(binaryDir, QueryUtils.getOS());
        PythonUtils.copyAndExtractScripts(scriptDir);
        PythonUtils.installPython(binaryDir, QueryUtils.getOS());
        var file1 = "wasp_bot.blend";
        var file2 = "refract_monkey.blend";
        var file3 = "bmw27_gpu.blend";
        var file4 = "scene-helicopter-27.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file2, file2);
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file3, file3);
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file4, file4);
        var blendfile1 = BlenderUtils.parseBlendFile(TEST_DIRECTORY + File.separator + file1, scriptDir, pythonDir);
        var blendfile2 = BlenderUtils.parseBlendFile(TEST_DIRECTORY + File.separator + file2, scriptDir, pythonDir);
        var blendfile3 = BlenderUtils.parseBlendFile(TEST_DIRECTORY + File.separator + file3, scriptDir, pythonDir);
        var blendfile4 = BlenderUtils.parseBlendFile(TEST_DIRECTORY + File.separator + file4, scriptDir, pythonDir);
        var blendfile5 = BlenderUtils.parseBlendFile("sample", scriptDir, pythonDir);
        assertThat(blendfile1).isNotNull();
        assertThat(blendfile2).isNotNull();
        assertThat(blendfile3).isNotNull();
        assertThat(blendfile4).isNotNull();
        assertThat(blendfile1.getEngine()).isEqualTo(BlenderEngine.BLENDER_EEVEE);
        assertThat(blendfile2.getEngine()).isEqualTo(BlenderEngine.BLENDER_RENDER);
        assertThat(blendfile3.getEngine()).isEqualTo(BlenderEngine.CYCLES);
        assertThat(blendfile4.getEngine()).isEqualTo(BlenderEngine.CYCLES);
        assertThat(blendfile5).isNull();
    }




    @Test
    void downloadBlenderToServer() {
        assertThat(BlenderUtils
                .downloadBlenderToServer("2.80b",
                        TEST_DIRECTORY.toString(),
                        OS.LINUX_64)).isNull();
        var blenderDownload = BlenderUtils
                .downloadBlenderToServer("2.79b",
                        TEST_DIRECTORY.toString(),
                        OS.LINUX_64);
        assertThat(blenderDownload).isNotNull();
        assertThat(blenderDownload).exists();
    }

    @Test
    void availableBlenderVersions() {
        assertThat(BlenderUtils.availableBlenderVersions()).hasSizeGreaterThan(0);
        System.out.println(BlenderUtils.availableBlenderVersions());
    }

    @Test
    void extractBlenderWindowsBinary() {
        var version = "2.83";
        var blenderDownload = BlenderUtils
                .downloadBlenderToServer(version,
                        TEST_DIRECTORY.toString(),
                        OS.WINDOWS_64);
        assertThat(BlenderUtils.extractBlender(TEST_DIRECTORY.toString(), OS.WINDOWS_64,
                blenderDownload.toString(), version)).isTrue();
        assertThat(blenderDownload).doesNotExist();
    }

    @Test
    void extractBlenderLinuxBinary() {
        var version = "2.83";
        var blenderDownload = BlenderUtils
                .downloadBlenderToServer(version,
                        TEST_DIRECTORY.toString(),
                        OS.LINUX_64);
        assertThat(BlenderUtils.extractBlender(TEST_DIRECTORY.toString(), OS.LINUX_64,
                blenderDownload.toString(), version)).isTrue();
        assertThat(blenderDownload).doesNotExist();
    }


    @Test
    @EnabledOnOs(MAC)
    void extractBlenderMacBinary() {
        var version = "2.83";
        var blenderDownload = BlenderUtils
                .downloadBlenderToServer(version,
                        TEST_DIRECTORY.toString(),
                        OS.MACOS);
        assertThat(BlenderUtils.extractBlender(TEST_DIRECTORY.toString(), OS.MACOS,
                blenderDownload.toString(), version)).isTrue();
        assertThat(blenderDownload).doesNotExist();
    }




}
