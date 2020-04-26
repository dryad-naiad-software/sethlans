package com.dryadandnaiad.sethlans.utils;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumableDownloadTest {

    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");
    static String FILE_URL = "http://ovh.net/files/1Mio.dat";
    static String FILE_NAME = TEST_DIRECTORY.toString() + File.separator + "file.dat";
    static String FILE_MD5_HASH = "6cb91af4ed4c60c11613b75cd1fc6116";

    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();

    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }


    @Test
    void downloadFile() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        ResumableDownload.downloadFile(FILE_URL, FILE_NAME);
        assertTrue(SethlansFileUtils.fileCheckMD5(new File(FILE_NAME), FILE_MD5_HASH));
    }

    @Test
    void downloadFileWithResume() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        ResumableDownload.downloadFileWithResume(FILE_URL, FILE_NAME);
        assertTrue(SethlansFileUtils.fileCheckMD5(new File(FILE_NAME), FILE_MD5_HASH));
    }
}