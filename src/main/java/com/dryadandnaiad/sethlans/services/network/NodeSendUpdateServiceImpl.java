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

import com.dryadandnaiad.sethlans.domains.database.queue.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/25/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NodeSendUpdateServiceImpl implements NodeSendUpdateService {
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private RenderTaskDatabaseService renderTaskDatabaseService;
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeSendUpdateServiceImpl.class);

    @Async
    @Override
    public void sendUpdateOnStart() {
        try {
            Thread.sleep(15000);
            sendRequest();
        } catch (InterruptedException e) {
            LOG.debug("Shutting Down Node Status Update Service");
        }
    }

    @Override
    @Async
    public void idleNodeNotification() {
        try {
            Thread.sleep(15000);
            LOG.debug("Starting Idle Notification Service.");
            int counter = 0;
            NodeInfo nodeInfo = SethlansUtils.getNodeInfo();
            ComputeType computeType = nodeInfo.getComputeType();
            int slots = 0;
            switch (computeType) {
                case CPU:
                    slots = 1;
                    break;
                case GPU:
                    if (nodeInfo.isCombined()) {
                        slots = 1;
                    } else {
                        slots = nodeInfo.getSelectedGPUs().size();
                    }
                    break;
                case CPU_GPU:
                    if (nodeInfo.isCombined()) {
                        slots = 2;
                    } else {
                        slots = nodeInfo.getSelectedGPUs().size() + 1;
                    }

            }
            while (true) {
                try {
                    Thread.sleep(1000);

                    if (sethlansServerDatabaseService.listActive().size() > 0 && blenderBenchmarkTaskDatabaseService.allBenchmarksComplete()) {
                        if (slots == renderTaskDatabaseService.listAll().size()) {
                            counter = 0;
                        }
                        if (slots > renderTaskDatabaseService.listAll().size()) {
                            counter++;
                        }
                        if (counter % 60 == 0 && counter > 59) {
                            LOG.debug("Node idle for " + (counter / 60) + " minutes");
                        }
                        if (counter > 899) {
                            LOG.debug("Informing server of idle slot(s)");
                            counter = 0;
                            if (slots == 1) {
                                sendIdleUpdate(computeType);
                            }
                            if (slots > 1) {
                                if (renderTaskDatabaseService.listAll().size() == 0) {
                                    sendIdleUpdate(computeType);
                                } else {
                                    List<ComputeType> computeTypeList = new ArrayList<>();
                                    for (RenderTask renderTask : renderTaskDatabaseService.listAll()) {
                                        computeTypeList.add(renderTask.getComputeType());
                                    }
                                    if (computeTypeList.contains(ComputeType.CPU)) {
                                        sendIdleUpdate(ComputeType.GPU);
                                    } else {
                                        sendIdleUpdate(ComputeType.CPU);

                                    }
                                }
                            }

                        }
                    }

                } catch (InterruptedException | BeanCreationNotAllowedException e) {
                    LOG.debug("Shutting Down Node Idle Notification Service");
                    break;
                }


            }
        } catch (InterruptedException e) {
            LOG.debug("Shutting Down Node Idle Notification Service");
        }


    }

    private void sendIdleUpdate(ComputeType computeType) {
        for (SethlansServer sethlansServer : sethlansServerDatabaseService.listAll()) {
            String url = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/update/node_idle_notification";
            String param = "connection_uuid=" + sethlansServer.getConnection_uuid() + "&compute_type=" + computeType;
            sethlansAPIConnectionService.sendToRemotePOST(url, param);
        }
    }




    private void sendRequest() {
        List<SethlansServer> sethlansServers = sethlansServerDatabaseService.listAll();
        if (!sethlansServers.isEmpty()) {
            for (SethlansServer sethlansServer : sethlansServers) {
                if (sethlansServer.isNodeUpdated()) {
                    LOG.debug("Sending node status update request to " + sethlansServer.getHostname());
                    String url = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/update/node_status_update/";
                    String param = "connection_uuid=" + sethlansServer.getConnection_uuid();
                    if (sethlansAPIConnectionService.sendToRemoteGET(url, param)) {
                        sethlansServer.setNodeUpdated(false);
                        sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
                    }
                }
            }
        } else {
            LOG.debug("No connections to Sethlans servers present.  No updates sent.");
        }
    }

    @Autowired
    public void setBlenderBenchmarkTaskDatabaseService(BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService) {
        this.blenderBenchmarkTaskDatabaseService = blenderBenchmarkTaskDatabaseService;
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
    public void setRenderTaskDatabaseService(RenderTaskDatabaseService renderTaskDatabaseService) {
        this.renderTaskDatabaseService = renderTaskDatabaseService;
    }
}
