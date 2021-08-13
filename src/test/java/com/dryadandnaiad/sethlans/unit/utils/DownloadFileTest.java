package com.dryadandnaiad.sethlans.unit.utils;

import com.dryadandnaiad.sethlans.utils.DownloadFile;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadFileTest {

    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");
    static String FILE_URL = "https://raw.githubusercontent.com/dryad-naiad-software/sethlans/master/LICENSE";
    static String FILE_NAME = TEST_DIRECTORY + File.separator + "file.txt";
    static String FILE_MD5_HASH = "e62f173a712a1f52dfd3f1047afb214b";

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
        DownloadFile.downloadFile(FILE_URL, FILE_NAME);
        Assertions.assertTrue(FileUtils.fileCheckMD5(new File(FILE_NAME), FILE_MD5_HASH));
    }

    @Test
    void downloadFileWithResume() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        DownloadFile.downloadFileWithResume(FILE_URL, FILE_NAME);
        assertTrue(FileUtils.fileCheckMD5(new File(FILE_NAME), FILE_MD5_HASH));
    }
}
