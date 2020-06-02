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

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static com.dryadandnaiad.sethlans.utils.ConfigUtils.*;

/**
 * Static methods that write and retrieve information from the sethlans.properties file
 * <p>
 * File created by Mario Estrella on 4/22/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class PropertiesUtils {

    public static List<GPU> getSelectedGPUs() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(ConfigUtils.getProperty(ConfigKeys.SELECTED_GPU),
                    new TypeReference<>() {
                    });
        } catch (JsonProcessingException e) {
            log.error("Error getting list of selected GPUs");
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }


    public static boolean getFirstTime() {
        return Boolean.parseBoolean(getProperty(ConfigKeys.FIRST_TIME));
    }


    public static SethlansMode getMode() {
        return SethlansMode.valueOf(getProperty(ConfigKeys.MODE));
    }

    public static void writeSetupSettings(SetupForm setupForm) throws Exception {
        writeProperty(ConfigKeys.MODE, setupForm.getMode().name());
        writeProperty(ConfigKeys.SETHLANS_IP, setupForm.getIpAddress());
        writeProperty(ConfigKeys.HTTPS_PORT, setupForm.getPort());
        writeProperty(ConfigKeys.SETHLANS_URL, setupForm.getAppURL());
        writeProperty(ConfigKeys.SYSTEM_ID, UUID.randomUUID().toString());
        writeProperty(ConfigKeys.LOG_LEVEL, setupForm.getLogLevel().toString());
        if (setupForm.getMode().equals(SethlansMode.SERVER) || setupForm.getMode().equals(SethlansMode.DUAL)) {
            writeProperty(ConfigKeys.GETTING_STARTED, "true");
        }

    }

    public static void writeNodeSettings(NodeSettings nodeSettings) throws Exception {
        writeProperty(ConfigKeys.NODE_TYPE, nodeSettings.getNodeType().toString());
        if (!nodeSettings.getNodeType().equals(NodeType.GPU)) {
            writeProperty(ConfigKeys.CPU_CORES, nodeSettings.getCores().toString());
            writeProperty(ConfigKeys.TILE_SIZE_CPU, nodeSettings.getTileSizeCPU().toString());
        } else {
            writeProperty(ConfigKeys.CPU_CORES, "0");
            writeProperty(ConfigKeys.TILE_SIZE_CPU, "0");
        }
        if (!nodeSettings.getNodeType().equals(NodeType.CPU)) {
            ObjectMapper objectMapper = new ObjectMapper();
            String selectedGPUs = objectMapper.writeValueAsString(nodeSettings.getSelectedGPUs());
            writeProperty(ConfigKeys.SELECTED_GPU, selectedGPUs);
            writeProperty(ConfigKeys.TILE_SIZE_GPU, nodeSettings.getTileSizeGPU().toString());
            writeProperty(ConfigKeys.COMBINE_GPU, Boolean.toString(nodeSettings.isGpuCombined()));
        } else {
            writeProperty(ConfigKeys.SELECTED_GPU, "{}");
            writeProperty(ConfigKeys.TILE_SIZE_GPU, "0");
        }


    }

    public static void writeMailSettings(MailSettings mailSettings) throws Exception {
        if (mailSettings.isMailEnabled()) {
            writeProperty(ConfigKeys.MAIL_SERVER_CONFIGURED, "true");
            if (mailSettings.isSmtpAuth()) {
                writeProperty(ConfigKeys.MAIL_USE_AUTH, "true");
                writeProperty(ConfigKeys.MAIL_USER, mailSettings.getUsername());
                writeEncryptedProperty(ConfigKeys.MAIL_PASS, mailSettings.getPassword());
            }
            writeProperty(ConfigKeys.MAIL_HOST, mailSettings.getMailHost());
            writeProperty(ConfigKeys.MAIL_PORT, mailSettings.getMailPort());
            writeProperty(ConfigKeys.MAIL_REPLY_TO, mailSettings.getReplyToAddress());
            writeProperty(ConfigKeys.MAIL_SSL_ENABLE, Boolean.toString(mailSettings.isSslEnabled()));
            writeProperty(ConfigKeys.MAIL_TLS_ENABLE, Boolean.toString(mailSettings.isStartTLSEnabled()));
            writeProperty(ConfigKeys.MAIL_TLS_REQUIRED, Boolean.toString(mailSettings.isStartTLSRequired()));
        }
    }

    public static void writeDirectories(SethlansMode mode) throws Exception {
        String rootDirectory = SystemUtils.USER_HOME + File.separator + ".sethlans";
        String scriptsDirectory = rootDirectory + File.separator + "scripts";
        String downloadDirectory = rootDirectory + File.separator + "downloads";
        String tempDirectory = rootDirectory + File.separator + "temp";
        String cacheDirectory = rootDirectory + File.separator + "render_cache";
        String blendFileCacheDirectory = rootDirectory + File.separator + "blendfile_cache";
        String logDirectory = rootDirectory + File.separator + "logs";
        String binDirectory = rootDirectory + File.separator + "bin";
        String benchmarkDirectory = rootDirectory + File.separator + "benchmarks";
        String projectDirectory = rootDirectory + File.separator + "projects";

        writeProperty(ConfigKeys.ROOT_DIR, rootDirectory);
        writeProperty(ConfigKeys.SCRIPTS_DIR, scriptsDirectory);
        writeProperty(ConfigKeys.TEMP_DIR, tempDirectory);
        writeProperty(ConfigKeys.BINARY_DIR, binDirectory);
        writeProperty(ConfigKeys.LOGGING_DIR, logDirectory);
        createDirectories(new File(binDirectory));
        createDirectories(new File(scriptsDirectory));
        createDirectories(new File(tempDirectory));


        if (mode.equals(SethlansMode.DUAL) || mode.equals(SethlansMode.SERVER)) {
            writeProperty(ConfigKeys.PROJECT_DIR, projectDirectory);
            writeProperty(ConfigKeys.DOWNLOAD_DIR, downloadDirectory);
            createDirectories(new File(projectDirectory));
            createDirectories(new File(downloadDirectory));
        }

        if (mode.equals(SethlansMode.DUAL) || mode.equals(SethlansMode.NODE)) {
            writeProperty(ConfigKeys.BENCHMARK_DIR, benchmarkDirectory);
            writeProperty(ConfigKeys.CACHE_DIR, cacheDirectory);
            writeProperty(ConfigKeys.BLEND_FILE_CACHE_DIR, blendFileCacheDirectory);
            createDirectories(new File(benchmarkDirectory));
            createDirectories(new File(cacheDirectory));
            createDirectories(new File(blendFileCacheDirectory));
        }
    }

    private static void createDirectories(File directory) {
        if (!directory.mkdirs()) {
            log.error("Unable to create directory" + directory);
        }

    }
}
