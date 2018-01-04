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

package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created Mario Estrella on 12/25/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NodeStatusUpdateServiceImpl implements NodeStatusUpdateService {
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeStatusUpdateServiceImpl.class);

    @Override
    @Async
    public void backgroundRequests() {
        while (true) {
            try {
                Thread.sleep(300000);
                nodeUpdatePullRequest();
            } catch (InterruptedException e) {
                LOG.debug("Stopping Node Status Update Service");
            }

        }
    }

    @Async
    @Override
    public void sendUpdateOnStart() {
        try {
            Thread.sleep(16000);
            nodeUpdatePullRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void nodeUpdatePullRequest() {
        List<BlenderBenchmarkTask> blenderBenchmarkTaskList = blenderBenchmarkTaskDatabaseService.listAll();
        if (!blenderBenchmarkTaskList.isEmpty()) {
            for (BlenderBenchmarkTask blenderBenchmarkTask : blenderBenchmarkTaskList) {
                if (blenderBenchmarkTask.isInProgress()) {
                    LOG.debug("A benchmark is in progress, node update requests on hold for 10 minutes.");
                    try {
                        Thread.sleep(600000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendRequest();
                }
            }
        } else {
            sendRequest();
        }


    }

    private void sendRequest() {
        List<SethlansServer> sethlansServers = sethlansServerDatabaseService.listAll();
        if (!sethlansServers.isEmpty()) {
            for (SethlansServer sethlansServer : sethlansServers) {
                LOG.debug("Sending node status update request to " + sethlansServer.getHostname());
                String url = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/update/node_status_update/";
                String param = "connection_uuid=" + sethlansServer.getConnection_uuid();
                sethlansAPIConnectionService.sendToRemoteGET(url, param);
            }
        } else {
            LOG.debug("No connections to Sethlans servers present.  No updates sent.");
        }
    }


    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderBenchmarkTaskDatabaseService(BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService) {
        this.blenderBenchmarkTaskDatabaseService = blenderBenchmarkTaskDatabaseService;
    }
}
