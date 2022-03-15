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

    BENCHMARK_DIR("sethlans.benchmarkDir"),
    BINARY_DIR("sethlans.binDir"),
    BLEND_FILE_CACHE_DIR("sethlans.blendFileCacheDir"),
    BLENDER_DEBUG("blender.debug"),
    BLENDER_EXECUTABLES("sethlans.blender.executables"),
    CACHE_DIR("sethlans.cacheDir"),
    COMBINE_GPU("sethlans.render.combined"),
    CONFIG_DIR("sethlans.configDir"),
    CPU_CORES("sethlans.cores"),
    CPU_RATING("sethlans.cpu.rating"),
    DOWNLOAD_DIR("sethlans.downloadDir"),
    FFMPEG_DIR("sethlans.ffmpegDir"),
    FIRST_TIME("sethlans.firstTime"),
    GETTING_STARTED("sethlans.getStartedWizard"),
    HTTPS_PORT("server.port"),
    LOG_LEVEL("logging.level.com.dryadandnaiad.sethlans"),
    LOGGING_DIR("sethlans.logDir"),
    LOGGING_FILE("logging.file"),
    MAIL_HOST("spring.mail.host"),
    MAIL_PASS("spring.mail.password"),
    MAIL_PORT("spring.mail.port"),
    MAIL_REPLY_TO("sethlans.mail.replyTo"),
    MAIL_SERVER_CONFIGURED("sethlans.configureMail"),
    MAIL_SSL_ENABLE("spring.mail.properties.mail.smtp.ssl.enable"),
    MAIL_TLS_ENABLE("spring.mail.properties.mail.smtp.starttls.enable"),
    MAIL_TLS_REQUIRED("spring.mail.properties.mail.smtp.starttls.required"),
    MAIL_USE_AUTH("spring.mail.properties.mail.smtp.auth"),
    MAIL_USER("spring.mail.username"),
    MODE("spring.profiles.active"),
    MULTICAST_IP("sethlans.multicast"),
    MULTICAST_PORT("sethlans.multicast.port"),
    NODE_DISABLED("sethlans.nodeDisabled"),
    NODE_TYPE("sethlans.nodeType"),
    NODE_TOTAL_SLOTS("sethlans.node.total.slots"),
    PROJECT_DIR("sethlans.projectDir"),
    PYTHON_DIR("sethlans.pythonDir"),
    ROOT_DIR("sethlans.rootDir"),
    SCRIPTS_DIR("sethlans.scriptsDir"),
    SERVER_COMPLETE_QUEUE_SIZE("sethlans.server.complete.queue.size"),
    SELECTED_GPU("sethlans.selectedGPU"),
    SESSION_TIMEOUT("server.session.timeout"),
    SETHLANS_IP("server.ipAddress"),
    SETHLANS_URL("sethlans.url"),
    SYSTEM_ID("sethlans.systemID"),
    TEMP_DIR("sethlans.tempDir"),
    TILE_SIZE_CPU("sethlans.tileSizeCPU"),
    TILE_SIZE_GPU("sethlans.tileSizeGPU"),
    USE_SETHLANS_CERT("sethlans.self-signed");


    private final String text;

    ConfigKeys(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
