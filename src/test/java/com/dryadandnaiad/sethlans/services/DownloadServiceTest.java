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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 6/10/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("SERVER")
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class DownloadServiceTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Resource
    BlenderArchiveRepository blenderArchiveRepository;

    @Autowired
    DownloadService downloadService;


    @BeforeAll
    static void beforeAll() throws Exception {
        var setupSettings = SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress(QueryUtils.getIP())
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.SERVER)
                .port("7443").build();
        PropertiesUtils.writeSetupSettings(setupSettings);
        PropertiesUtils.writeDirectories(SethlansMode.SERVER);
        var mailSettings = MailSettings.builder()
                .mailEnabled(true)
                .mailHost("localhost")
                .mailPort("25")
                .replyToAddress("noreply@test.com")
                .smtpAuth(true)
                .username("test_username@email.local")
                .password("litter")
                .build();
        PropertiesUtils.writeMailSettings(mailSettings);
    }

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }


    @Test
    void downloadBlenderFilesAsync() throws InterruptedException {
        blenderArchiveRepository.save(BlenderArchive.builder()
                .blenderOS(QueryUtils.getOS())
                .downloaded(false)
                .blenderVersion("2.79b")
                .build());

        var blenderBinary = blenderArchiveRepository.findAll().get(0);
        assertThat(blenderBinary).isNotNull();
        Thread.sleep(11000);
        while (!blenderBinary.isDownloaded()) {
            blenderBinary = blenderArchiveRepository.findAll().get(0);
            Thread.sleep(1000);
            assertThat(new File(SETHLANS_DIRECTORY + File.separator + "downloads")).isNotEmptyDirectory();
        }
        assertThat(new File(SETHLANS_DIRECTORY + File.separator + "downloads")).isNotEmptyDirectory();
    }
}
