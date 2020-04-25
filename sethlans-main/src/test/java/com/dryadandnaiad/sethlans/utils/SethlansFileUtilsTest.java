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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
        printWriter.println(RandomStringUtils.random(4096, true, false));
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
        printWriter.println(RandomStringUtils.random(4096, true, false));
        printWriter.close();
        assertThat(SethlansFileUtils.getMD5ofFile(file)).isNotNull();
        file.delete();
    }

    @Test
    void createImageIcon() {
        String image = "images/sethlans_systray.png";
        var imageIcon = SethlansFileUtils.createImageIcon(image, "Test Image");
        assertThat(imageIcon).isNotNull();
        assertThat(imageIcon).isInstanceOf(Image.class);
    }

    @Test
    void createArchive() throws IOException {
        var archiveName = "testFile";
        var zipFile = new File(testDirectory + File.separator + archiveName + ".zip");
        assertThat(zipFile).doesNotExist();
        var file1 = new File(testDirectory + File.separator + "sample.txt");
        var file2 = new File(testDirectory + File.separator + "sample1.txt");
        var file3 = new File(testDirectory + File.separator + "sample2.txt");
        file1.createNewFile();
        file2.createNewFile();
        file3.createNewFile();
        var fileWriter1 = new FileWriter(file1);
        var printWriter1 = new PrintWriter(fileWriter1);
        var fileWriter2 = new FileWriter(file2);
        var printWriter2 = new PrintWriter(fileWriter2);
        var fileWriter3 = new FileWriter(file3);
        var printWriter3 = new PrintWriter(fileWriter3);
        printWriter1.println(RandomStringUtils.random(4096, true, false));
        printWriter1.close();
        printWriter2.println(RandomStringUtils.random(4096, true, false));
        printWriter2.close();
        printWriter3.println(RandomStringUtils.random(4096, true, false));
        printWriter3.close();
        var files = new ArrayList<String>();
        files.add(file1.toString());
        files.add(file2.toString());
        files.add(file3.toString());
        var archive = SethlansFileUtils.createArchive(files, testDirectory.toString(), archiveName);
        assertThat(archive.toString()).isEqualTo(testDirectory + File.separator + archiveName + ".zip");
        assertThat(archive.toString());
        file1.delete();
        file2.delete();
        file3.delete();
        zipFile.delete();
    }
}
