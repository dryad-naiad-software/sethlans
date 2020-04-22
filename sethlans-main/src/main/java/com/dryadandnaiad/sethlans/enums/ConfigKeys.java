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
 * Created by Mario Estrella on 4/20/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public enum ConfigKeys {
    ACCESS_KEY("sethlans.accessKey"),
    SETHLANS_IP("server.ipAddress"),
    HTTPS_PORT("server.port"),
    FIRST_TIME("sethlans.firstTime"),
    LOGGING_FILE("logging.file"),
    MODE("sethlans.mode"),
    COMPUTE_METHOD("sethlans.computeMethod"),
    PROJECT_DIR("sethlans.projectDir"),
    LOGGING_DIR("sethlans.logDir"),
    BLENDER_DIR("sethlans.blenderDir"),
    BINARY_DIR("sethlans.binDir"),
    TEMP_DIR("sethlans.tempDir"),
    CACHE_DIR("sethlans.cacheDir"),
    CONFIG_DIR("sethlans.configDir"),
    BLEND_FILE_CACHE_DIR("sethlans.blendFileCacheDir"),
    FFMPEG_BIN("sethlans.ffmpeg.binary"),
    CACHED_BLENDER_BINARIES("sethlans.cachedBlenderBinaries"),
    GPU_DEVICE("sethlans.gpu_id"),
    COMBINE_GPU("sethlans.render.combined"),
    CPU_CORES("sethlans.cores"),
    PYTHON_BIN("sethlans.python.binary"),
    SCRIPTS_DIR("sethlans.scriptsDir"),
    TILE_SIZE_CPU("sethlans.tileSizeCPU"),
    TILE_SIZE_GPU("sethlans.tileSizeGPU"),
    BENCHMARK_DIR("sethlans.benchmarkDir"),
    ROOT_DIR("sethlans.rootDir"),
    SESSION_TIMEOUT("server.session.timeout"),
    LOG_LEVEL("logging.level.com.dryadandnaiad.sethlans"),
    DATABASE_LOCATION("spring.datasource.url"),
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
