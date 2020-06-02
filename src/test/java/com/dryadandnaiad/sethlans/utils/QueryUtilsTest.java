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

import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.models.blender.project.ImageSettings;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectSettings;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * File created by Mario Estrella on 4/19/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class QueryUtilsTest {

    @Test
    void getShortUUID() {
        val shortUUID = QueryUtils.getShortUUID();
        assertThat(shortUUID).hasSize(13);
        assertThat(shortUUID).contains("-");
    }

    @Test
    void getIP() {
        val ipAddress = QueryUtils.getIP();
        val validator = InetAddressValidator.getInstance();
        assertNotNull(ipAddress);
        assertThat(ipAddress).isNotEqualTo("0.0.0.0");
        assertThat(ipAddress).isNotEqualTo("255.255.255.255");
        assertThat(ipAddress).isNotEqualTo("127.0.0.1");
        assertTrue("Not a valid IP address", validator.isValidInet4Address(ipAddress));
    }

    @Test
    void getOS() {
        val os = QueryUtils.getOS();
        assertNotNull(os);
        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(os.getName()).contains("Windows");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(os.getName()).contains("Linux");
        }
        if (SystemUtils.IS_OS_MAC) {
            assertThat(os.getName()).contains("MacOS");
        }

    }

    @Test
    void getHostname() {
        val hostname = QueryUtils.getHostname();
        assertNotNull(hostname);
        assertThat(hostname).doesNotContain(".");
        assertThat(hostname).isUpperCase();
    }

    @Test
    void getAvailableMethods() {
        val availableMethods = QueryUtils.getAvailableTypes();
        assertNotNull(availableMethods);
        assertThat(availableMethods).hasSizeGreaterThan(0);
        if (availableMethods.size() > 1) {
            assertThat(availableMethods).contains(NodeType.GPU);
        } else {
            assertThat(availableMethods.contains(NodeType.CPU));
        }
    }

    @Test
    void getCurrentSystemInfo() {
        val systemInfo = QueryUtils.getCurrentSystemInfo();
        assertThat(systemInfo).isNotNull();
    }

    @Test
    void getVersion() {
        val version = QueryUtils.getVersion();
        assertNotNull(version);
    }

    @Test
    void getTimeFromMills() {
        val time = QueryUtils.getTimeFromMills(9000000L);
        assertThat(time.equals("02:30:00"));
    }


    @Test
    void readStringFromFile() {
        assertThat(QueryUtils.getStringFromFile("test")).isNull();
    }

    @Test
    void readStringFromResource() {
        assertThat(QueryUtils.getStringFromResource("blenderdownload.json")).isNotNull();
        assertThat(QueryUtils.getStringFromResource("cookie")).isNull();
    }

    @Test
    void getFrameAndPartFilename() {
        var imageSettings = ImageSettings.builder().imageOutputFormat(ImageOutputFormat.PNG).build();
        var projectSettings = ProjectSettings.builder().imageSettings(imageSettings).build();
        var project = Project.builder()
                .projectName("An example of a Project")
                .projectID("92fb0f42-46b3-476c-b4b9-3d2b7d80e6e9")
                .projectSettings(projectSettings)
                .build();
        assertThat(QueryUtils.getFrameAndPartFilename(project, 1, 2))
                .isEqualTo("anexample-92fb-0001-2.png");
        assertThat(QueryUtils.getFrameAndPartFilename(project, 1, null))
                .isEqualTo("anexample-92fb-0001.png");
        imageSettings.setImageOutputFormat(ImageOutputFormat.TIFF);
        project.getProjectSettings().setImageSettings(imageSettings);
        assertThat(QueryUtils.getFrameAndPartFilename(project, 1, 2))
                .isEqualTo("anexample-92fb-0001-2.tif");
        assertThat(QueryUtils.getFrameAndPartFilename(project, 1, null))
                .isEqualTo("anexample-92fb-0001.tif");
        imageSettings.setImageOutputFormat(ImageOutputFormat.HDR);
        project.getProjectSettings().setImageSettings(imageSettings);
        assertThat(QueryUtils.getFrameAndPartFilename(project, 1, 2))
                .isEqualTo("anexample-92fb-0001-2.hdr");
        assertThat(QueryUtils.getFrameAndPartFilename(project, 1, null))
                .isEqualTo("anexample-92fb-0001.hdr");
    }
}
