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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.springframework.util.FileSystemUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * File created by Mario Estrella on 4/23/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class FileUtilsTest {

    File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");

    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @Test
    void isDirectoryEmpty() throws IOException {
        assertThat(FileUtils.isDirectoryEmpty(TEST_DIRECTORY)).isTrue();
        var file = new File(TEST_DIRECTORY + File.separator + "sample.txt");
        file.createNewFile();
        assertThat(FileUtils.isDirectoryEmpty(TEST_DIRECTORY)).isFalse();
    }

    @Test
    void fileCheckMD5() throws IOException {
        var file = new File(TEST_DIRECTORY + File.separator + "sample.txt");
        file.createNewFile();
        var fileWriter = new FileWriter(file);
        var printWriter = new PrintWriter(fileWriter);
        printWriter.println(RandomStringUtils.random(4096, true, false));
        printWriter.close();
        var md5 = FileUtils.getMD5ofFile(file);
        assertThat(FileUtils.fileCheckMD5(file, md5)).isTrue();
    }

    @Test
    void getMD5ofFile() throws IOException {
        var file = new File(TEST_DIRECTORY + File.separator + "sample.txt");
        file.createNewFile();
        var fileWriter = new FileWriter(file);
        var printWriter = new PrintWriter(fileWriter);
        printWriter.println(RandomStringUtils.random(4096, true, false));
        printWriter.close();
        assertThat(FileUtils.getMD5ofFile(file)).isNotNull();
    }

    @Test
    void createImageIcon() {
        String image = "images/sethlans_systray.png";
        var imageIcon = FileUtils.createImageIcon(image, "Test Image");
        assertThat(imageIcon).isNotNull();
        assertThat(imageIcon).isInstanceOf(Image.class);
    }

    @Test
    void createZipArchive() throws IOException {
        var archiveName = "testFile";
        var zipFile = new File(TEST_DIRECTORY + File.separator + archiveName + ".zip");
        assertThat(zipFile).doesNotExist();
        var files = createFiles();
        var archive = FileUtils.createZipArchive(files, TEST_DIRECTORY.toString(), archiveName);
        assertThat(archive.toString()).isEqualTo(TEST_DIRECTORY + File.separator + archiveName + ".zip");
        assertThat(archive.toString());
    }

    @Test
    void extractArchive() throws IOException {
        var archiveDir = new File(TEST_DIRECTORY + File.separator + "extracted");

        var xzArchive = makeTxzArchive();
        assertThat(xzArchive).exists();
        assertThat(FileUtils.extractArchive(xzArchive.toString(), archiveDir.toString())).isTrue();
        assertThat(FileUtils.isDirectoryEmpty(archiveDir)).isFalse();

        var gzArchive = makeTarGzArchive();
        assertThat(gzArchive).exists();
        assertThat(FileUtils.extractArchive(gzArchive.toString(), archiveDir.toString())).isTrue();
        assertThat(FileUtils.isDirectoryEmpty(archiveDir)).isFalse();

        var bz2Archive = makeTarBz2Archive();
        assertThat(bz2Archive).exists();
        assertThat(FileUtils.extractArchive(bz2Archive.toString(), archiveDir.toString())).isTrue();
        assertThat(FileUtils.isDirectoryEmpty(archiveDir)).isFalse();

        var files = createFiles();
        var zipArchive = FileUtils.createZipArchive(files, TEST_DIRECTORY.toString(), "zipArchive");
        assertThat(FileUtils.extractArchive(zipArchive.toString(), archiveDir.toString())).isTrue();
        assertThat(FileUtils.isDirectoryEmpty(archiveDir)).isFalse();

        var sevenZArchive = make7z();
        assertThat(sevenZArchive).exists();
        assertThat(FileUtils.extractArchive(sevenZArchive.toString(), archiveDir.toString())).isFalse();
    }


    @Test
    void getExtensionFromString() {
        var toTest = "regular.file.with.multiple.dots.txt";
        var toTest2 = "string.with.multiple.dots.tar.gz";
        assertThat(FileUtils.getExtensionFromString(toTest)).isEqualTo(".txt");
        assertThat(FileUtils.getExtensionFromString(toTest2)).isEqualTo(".tar.gz");
    }

    @Test
    void removeExtensionFromString() {
        var toTest = "blender-2.83.2-macos.dmg";
        var toTest2 = "blender-2.83.2-linux.tar.gz";
        assertThat(FileUtils.removeExtensionFromString(toTest)).isEqualTo("blender-2.83.2-macos");
        assertThat(FileUtils.removeExtensionFromString(toTest2)).isEqualTo("blender-2.83.2-linux");
    }

    @Test
    void listFiles() throws IOException {
        createFiles();
        new File(TEST_DIRECTORY + File.separator + "testDir").mkdirs();
        var list = FileUtils.listFiles(TEST_DIRECTORY.toString());
        assertThat(list).doesNotContain("testDir");
        assertThat(list).hasSize(3);
    }

    @Test
    void listDirectories() throws IOException {
        createFiles();
        new File(TEST_DIRECTORY + File.separator + "testDir").mkdirs();
        var list = FileUtils.listDirectories(TEST_DIRECTORY.toString());
        assertThat(list).containsOnly("testDir");
    }

    private File make7z() throws IOException {
        var name = "archive.tar.7z";
        File archive = new File(TEST_DIRECTORY + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.SEVEN_Z);
        archiver.create(name, TEST_DIRECTORY, fileListArray());
        return archive;
    }

    private File makeTxzArchive() throws IOException {
        var name = "archive.tar.xz";
        File archive = new File(TEST_DIRECTORY + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
        archiver.create(name, TEST_DIRECTORY, fileListArray());
        return archive;
    }

    private File makeTarGzArchive() throws IOException {
        var name = "archive.tar.gz";
        File archive = new File(TEST_DIRECTORY + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
        archiver.create(name, TEST_DIRECTORY, fileListArray());
        return archive;
    }

    private File makeTarBz2Archive() throws IOException {
        var name = "archive.tar.bz2";
        File archive = new File(TEST_DIRECTORY + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
        archiver.create(name, TEST_DIRECTORY, fileListArray());
        return archive;
    }

    private List<String> createFiles() throws IOException {
        var file1 = new File(TEST_DIRECTORY + File.separator + "sample.txt");
        var file2 = new File(TEST_DIRECTORY + File.separator + "sample1.txt");
        var file3 = new File(TEST_DIRECTORY + File.separator + "sample2.txt");
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
        return files;

    }

    private File[] fileListArray() throws IOException {
        var filesToArchive = createFiles();
        var fileArray = new File[3];
        for (int i = 0; i < 3; i++) {
            fileArray[i] = new File(filesToArchive.get(i));
        }
        return fileArray;
    }


}
