/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Profile({"SERVER", "NODE", "DUAL"})
public class BlenderBenchmarkServiceImpl implements BlenderBenchmarkService {
    @Value("${sethlans.tempDir}")
    private String tempDir;

    @Value("${sethlans.primaryBlenderVersion}")
    private String primaryBlenderVersion;

    private static final Logger LOG = LoggerFactory.getLogger(BlenderBenchmarkServiceImpl.class);

    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private SethlansServerDatabaseService sethlansServerDatabaseService;

    @Override
    public void sendBenchmarktoNode(SethlansNode sethlansNode) {
        String nodeURL = "https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() + "/api/benchmark/request";
        String params = "connection_uuid=" + sethlansNode.getConnection_uuid() + "&compute_type=" + sethlansNode.getComputeType() + "&blender_version=" + primaryBlenderVersion;
        sethlansAPIConnectionService.sendToRemotePOST(nodeURL, params);

    }

    @Override
    @Async
    public void processReceivedBenchmark(String benchmark_uuid) {
        BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        // Process benchmark

    }

    @Override
    @Async
    public void processReceivedBenchmarks(List<String> benchmark_uuids) {
        for (String benchmark : benchmark_uuids) {
            BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark);
            LOG.debug("Processing benchmark task: \n" + benchmarkTask.toString());
            File benchmarkDir = new File(tempDir + File.separator + benchmarkTask.getBenchmark_uuid() + "_" + benchmarkTask.getBenchmarkURL());
            if (downloadRequiredFiles(benchmarkDir, benchmarkTask)) {

            }
            // Process benchmark;
        }

    }

    @Override
    public void sendResultsToServer(SethlansServer sethlansServer) {

    }

    private boolean downloadRequiredFiles(File benchmarkDir, BlenderBenchmarkTask benchmarkTask) {
        LOG.debug("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(benchmarkTask.getConnection_uuid());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();

        if (benchmarkDir.mkdirs()) {

            //Download Blender from server
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
            String params = "?connection_uuid=" + benchmarkTask.getConnection_uuid() + "&version=" + benchmarkTask.getBlenderVersion() + "&os=" + SethlansUtils.getOS();
            String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            SethlansUtils.archiveExtract(filename, benchmarkDir);
            renameBlender(benchmarkDir);
            benchmarkTask.setBlenderExecutable(assignBlenderExecutable(benchmarkDir));

            // Download benchmark from server
            connectionURL = "https://" + serverIP + ":" + serverPort + "/api/benchmark_files/" + benchmarkTask.getBenchmarkURL();
            params = "?connection_uuid=" + benchmarkTask.getConnection_uuid();
            String benchmarkFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            benchmarkTask.setBenchmarkFile(benchmarkFile);
            LOG.debug("Required files downloaded, updated benchmark task saved to database. \n" + benchmarkTask);
            blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
            return true;

        }
        return false;
    }

    private void renameBlender(File benchmarkDir) {
        FileFilter fileFilter = new WildcardFileFilter("blender*");
        File[] files = benchmarkDir.listFiles(fileFilter);
        for (File file : files) {
            if (file.isDirectory()) {
                file.renameTo(new File(benchmarkDir + File.separator + "blender"));
            }
        }
    }

    private String assignBlenderExecutable(File benchmarkDir) {
        String executable = null;
        if (SethlansUtils.getOS().equals("MacOS")) {
            executable = benchmarkDir.toString() + File.separator + "blender/blender.app/Contents/MacOS/blender";
        }
        LOG.debug("Setting executable to: " + executable);
        return executable;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderBenchmarkTaskDatabaseService(BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService) {
        this.blenderBenchmarkTaskDatabaseService = blenderBenchmarkTaskDatabaseService;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }
}
