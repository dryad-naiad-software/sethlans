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

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Mario Estrella on 4/23/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class SethlansFileUtilsTest {

    File testDirectory;

    @BeforeEach
    void setUp() {
        testDirectory = new File(SystemUtils.USER_HOME + File.separator + "testing");
        testDirectory.mkdirs();

    }

    @AfterEach
    void tearDown() {
        testDirectory.delete();
    }

    @Test
    void isDirectoryEmpty() throws IOException {
        assertThat(SethlansFileUtils.isDirectoryEmpty(testDirectory)).isTrue();
        var file = new File(testDirectory + File.separator + "sample.txt");
        file.createNewFile();
        assertThat(SethlansFileUtils.isDirectoryEmpty(testDirectory)).isFalse();
        file.delete();
    }

    @Test
    void fileCheckMD5() throws IOException, NoSuchAlgorithmException {
        var file = new File(testDirectory + File.separator + "sample.txt");
        file.createNewFile();
        var fileWriter = new FileWriter(file);
        var printWriter = new PrintWriter(fileWriter);
        printWriter.println("This is a test");
        printWriter.println("Another line");
        printWriter.close();
        var md5 = SethlansFileUtils.getMD5ofFile(file);
        assertThat(SethlansFileUtils.fileCheckMD5(file, md5)).isTrue();
        file.delete();
    }

    @Test
    void getMD5ofFile() throws IOException, NoSuchAlgorithmException {
        var file = new File(testDirectory + File.separator + "sample.txt");
        file.createNewFile();
        var fileWriter = new FileWriter(file);
        var printWriter = new PrintWriter(fileWriter);
        printWriter.println("This is a test");
        printWriter.println("Another line");
        printWriter.close();
        assertThat(SethlansFileUtils.getMD5ofFile(file)).isNotNull();
        file.delete();
    }

    @Test
    void createImage() {
    }

    @Test
    void createArchive() {
    }
}
