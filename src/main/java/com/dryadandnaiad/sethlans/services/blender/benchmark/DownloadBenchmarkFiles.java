/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.blender.benchmark;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.dryadandnaiad.sethlans.utils.BlenderUtils.assignBlenderExecutable;
import static com.dryadandnaiad.sethlans.utils.BlenderUtils.renameBlenderDir;
import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;
import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.archiveExtract;

/**
 * Created Mario Estrella on 9/1/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class DownloadBenchmarkFiles {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadBenchmarkFiles.class);

    static boolean downloadRequiredFiles(File benchmarkDir, BlenderBenchmarkTask benchmarkTask,
                                         SethlansAPIConnectionService sethlansAPIConnectionService,
                                         SethlansServerDatabaseService sethlansServerDatabaseService) {
        String binDir = SethlansConfigUtils.getProperty(SethlansConfigKeys.BINARY_DIR);
        LOG.debug("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(benchmarkTask.getConnection_uuid());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();
        String cachedBlenderBinaries = getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES);

        if (benchmarkDir.mkdirs()) {
            String[] cachedBinariesList;
            boolean versionCached = false;
            cachedBinariesList = cachedBlenderBinaries.split(",");
            for (String binary : cachedBinariesList) {
                if (binary.equals(benchmarkTask.getBlenderVersion())) {
                    versionCached = true;
                    LOG.debug(binary + " renderer is already cached.  Skipping Download.");

                }
            }
            if (!versionCached) {
                //Download Blender from server
                String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
                String params = "connection_uuid=" + benchmarkTask.getConnection_uuid() + "&version=" + benchmarkTask.getBlenderVersion() + "&os=" + SethlansQueryUtils.getOS();
                String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, binDir);
                while (filename.isEmpty()) {
                    try {
                        LOG.info("Was unable to get blender binary, retrying in 60 seconds");
                        Thread.sleep(60000);
                        filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, binDir);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (archiveExtract(filename, new File(binDir))) {
                    LOG.debug("Extraction complete.");
                    LOG.debug("Attempting to rename blender directory. Will attempt 3 tries.");
                    int count = 0;
                    while (!renameBlenderDir(benchmarkDir, new File(binDir), benchmarkTask, cachedBlenderBinaries)) {
                        count++;
                        if (count == 2) {
                            return false;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                benchmarkTask.setBenchmarkDir(benchmarkDir.toString());
                benchmarkTask.setBlenderExecutable(assignBlenderExecutable(new File(binDir), benchmarkTask.getBlenderVersion()));
            }


            // Download benchmark from server
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/benchmark_files/" + benchmarkTask.getBenchmarkURL() + "/";
            String params = "connection_uuid=" + benchmarkTask.getConnection_uuid();
            String benchmarkFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            benchmarkTask.setBenchmarkFile(benchmarkFile);
            LOG.debug("Required files downloaded.");
            return true;

        }
        return false;
    }
}
