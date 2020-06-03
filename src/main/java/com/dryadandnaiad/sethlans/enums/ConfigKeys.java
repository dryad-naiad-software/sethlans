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

package com.dryadandnaiad.sethlans.enums;

/**
 * File created by Mario Estrella on 4/20/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public enum ConfigKeys {
    SYSTEM_ID("sethlans.systemID"),
    SETHLANS_IP("server.ipAddress"),
    HTTPS_PORT("server.port"),
    FIRST_TIME("sethlans.firstTime"),
    LOGGING_FILE("logging.file"),
    MODE("spring.profiles.active"),
    BLENDER_DOWNLOAD_JSON_LOCATION("sethlans.blender.json.location"),
    NODE_TYPE("sethlans.nodeType"),
    PROJECT_DIR("sethlans.projectDir"),
    LOGGING_DIR("sethlans.logDir"),
    SCRIPTS_DIR("sethlans.scriptsDir"),
    DOWNLOAD_DIR("sethlans.downloadDir"),
    BENCHMARK_DIR("sethlans.benchmarkDir"),
    BINARY_DIR("sethlans.binDir"),
    ROOT_DIR("sethlans.rootDir"),
    TEMP_DIR("sethlans.tempDir"),
    CACHE_DIR("sethlans.cacheDir"),
    CONFIG_DIR("sethlans.configDir"),
    BLEND_FILE_CACHE_DIR("sethlans.blendFileCacheDir"),
    PYTHON_BIN("sethlans.python.binary"),
    FFMPEG_BIN("sethlans.ffmpeg.binary"),
    SELECTED_GPU("sethlans.selectedGPU"),
    COMBINE_GPU("sethlans.render.combined"),
    CPU_CORES("sethlans.cores"),
    TILE_SIZE_CPU("sethlans.tileSizeCPU"),
    TILE_SIZE_GPU("sethlans.tileSizeGPU"),
    SESSION_TIMEOUT("server.session.timeout"),
    LOG_LEVEL("logging.level.com.dryadandnaiad.sethlans"),
    MAIL_HOST("spring.mail.host"),
    MAIL_PORT("spring.mail.port"),
    MAIL_USER("spring.mail.username"),
    MAIL_PASS("spring.mail.password"),
    MAIL_SSL_ENABLE("spring.mail.properties.mail.smtp.ssl.enable"),
    MAIL_TLS_ENABLE("spring.mail.properties.mail.smtp.starttls.enable"),
    MAIL_TLS_REQUIRED("spring.mail.properties.mail.smtp.starttls.required"),
    MAIL_USE_AUTH("spring.mail.properties.mail.smtp.auth"),
    MAIL_SERVER_CONFIGURED("sethlans.configureMail"),
    MAIL_REPLY_TO("sethlans.mail.replyTo"),
    GETTING_STARTED("sethlans.getStartedWizard"),
    SETHLANS_URL("sethlans.url"),
    BLENDER_DEBUG("blender.debug");


    private final String text;

    ConfigKeys(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
