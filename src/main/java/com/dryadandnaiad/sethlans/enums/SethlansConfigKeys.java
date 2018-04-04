/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.enums;

/**
 * Created Mario Estrella on 3/18/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public enum SethlansConfigKeys {
    HTTPS_PORT("server.port"),
    FIRST_TIME("sethlans.firsttime"),
    LOGGING_FILE("logging.file"),
    MODE("sethlans.mode"),
    COMPUTE_METHOD("sethlans.computeMethod"),
    PRIMARY_BLENDER_VERSION("sethlans.primaryBlenderVersion"),
    PROJECT_DIR("sethlans.projectDir"),
    BLENDER_DIR("sethlans.blenderDir"),
    BINARY_DIR("sethlans.binDir"),
    TEMP_DIR("sethlans.tempDir"),
    CACHE_DIR("sethlans.cacheDir"),
    CACHED_BLENDER_BINARIES("sethlans.cachedBlenderBinaries"),
    GPU_DEVICE("sethlans.gpu_id"),
    CPU_CORES("sethlans.cores"),
    PYTHON_BIN("sethlans.python.binary"),
    SCRIPTS_DIR("sethlans.scriptsDir"),
    SETHLANS_IP("server.ipaddress"),
    TILE_SIZE_CPU("sethlans.tileSizeCPU"),
    TILE_SIZE_GPU("sethlans.tileSizeGPU"),
    BENCHMARK_DIR("sethlans.benchmarkDir"),
    ROOT_DIR("sethlans.rootDir"),
    SESSION_TIMEOUT("server.session.timeout");


    private final String text;

    SethlansConfigKeys(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}