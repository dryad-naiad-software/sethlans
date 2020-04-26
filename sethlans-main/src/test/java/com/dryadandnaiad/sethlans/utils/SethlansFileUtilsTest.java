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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Mario Estrella on 4/23/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
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
    void createZipArchive() throws IOException {
        var archiveName = "testFile";
        var zipFile = new File(testDirectory + File.separator + archiveName + ".zip");
        assertThat(zipFile).doesNotExist();
        var files = createFiles();
        var archive = SethlansFileUtils.createZipArchive(files, testDirectory.toString(), archiveName);
        assertThat(archive.toString()).isEqualTo(testDirectory + File.separator + archiveName + ".zip");
        assertThat(archive.toString());
        deleteFiles();
        zipFile.delete();
    }

    @Test
    void extractArchive() throws IOException {
        var archiveDir = new File(testDirectory + File.separator + "extracted");

        var xzArchive = makeTxzArchive();
        deleteFiles();
        assertThat(xzArchive).exists();
        assertThat(SethlansFileUtils.extractArchive(xzArchive, archiveDir, true)).isTrue();
        assertThat(SethlansFileUtils.isDirectoryEmpty(archiveDir)).isFalse();
        FileSystemUtils.deleteRecursively(archiveDir);

        var gzArchive = makeTarGzArchive();
        deleteFiles();
        assertThat(gzArchive).exists();
        assertThat(SethlansFileUtils.extractArchive(gzArchive, archiveDir, true)).isTrue();
        assertThat(SethlansFileUtils.isDirectoryEmpty(archiveDir)).isFalse();
        FileSystemUtils.deleteRecursively(archiveDir);

        var bz2Archive = makeTarBz2Archive();
        deleteFiles();
        assertThat(bz2Archive).exists();
        assertThat(SethlansFileUtils.extractArchive(bz2Archive, archiveDir, true)).isTrue();
        assertThat(SethlansFileUtils.isDirectoryEmpty(archiveDir)).isFalse();
        FileSystemUtils.deleteRecursively(archiveDir);

        var files = createFiles();
        var zipArchive = SethlansFileUtils.createZipArchive(files, testDirectory.toString(), "zipArchive");
        deleteFiles();
        assertThat(SethlansFileUtils.extractArchive(zipArchive, archiveDir, true)).isTrue();
        assertThat(SethlansFileUtils.isDirectoryEmpty(archiveDir)).isFalse();
        FileSystemUtils.deleteRecursively(archiveDir);

        var sevenZArchive = make7z();
        deleteFiles();
        assertThat(sevenZArchive).exists();
        assertThat(SethlansFileUtils.extractArchive(sevenZArchive, archiveDir, true)).isFalse();
        sevenZArchive.delete();
    }

    @Test
    void extractBlenderFromDMG() {
    }

    private File make7z() throws IOException {
        var name = "archive.tar.7z";
        File archive = new File(testDirectory + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.SEVEN_Z);
        archiver.create(name, testDirectory, fileListArray());
        return archive;
    }

    private File makeTxzArchive() throws IOException {
        var name = "archive.tar.xz";
        File archive = new File(testDirectory + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
        archiver.create(name, testDirectory, fileListArray());
        return archive;
    }

    private File makeTarGzArchive() throws IOException {
        var name = "archive.tar.gz";
        File archive = new File(testDirectory + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
        archiver.create(name, testDirectory, fileListArray());
        return archive;
    }

    private File makeTarBz2Archive() throws IOException {
        var name = "archive.tar.bz2";
        File archive = new File(testDirectory + File.separator + name);
        Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
        archiver.create(name, testDirectory, fileListArray());
        return archive;
    }

    private List<String> createFiles() throws IOException {
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
        return files;

    }

    private void deleteFiles() {
        var file1 = new File(testDirectory + File.separator + "sample.txt");
        var file2 = new File(testDirectory + File.separator + "sample1.txt");
        var file3 = new File(testDirectory + File.separator + "sample2.txt");
        file1.delete();
        file2.delete();
        file3.delete();
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
