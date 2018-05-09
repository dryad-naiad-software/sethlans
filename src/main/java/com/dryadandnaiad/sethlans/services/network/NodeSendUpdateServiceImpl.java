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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
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
public class NodeSendUpdateServiceImpl implements NodeSendUpdateService {
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService;
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
        } catch (Exception e) {
            LOG.error("Unknown Exception caught, catching and logging");
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));

        }
    }

    /**
     * Sends an update if the node has been idle too long
     */
    @Override
    @Async
    public void idleNodeNotification() {
        try {
            Thread.sleep(15000);
            int counter = 0;
            ComputeType computeType = ComputeType.valueOf(SethlansUtils.getProperty(SethlansConfigKeys.COMPUTE_METHOD.toString()));
            int slots;
            if (computeType.equals(ComputeType.CPU_GPU)) {
                slots = 2;
            } else {
                slots = 1;
            }
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (sethlansServerDatabaseService.listActive().size() > 0 && blenderBenchmarkTaskDatabaseService.listAll().size() == 0) {
                        if (blenderRenderTaskDatabaseService.listAll().size() == 0) {
                            counter++;
                        }
                        if (slots == 2 && blenderRenderTaskDatabaseService.listAll().size() == 1) {
                            counter++;
                        }
                        if (slots == 2 && blenderRenderTaskDatabaseService.listAll().size() == 2) {
                            counter = 0;
                        }
                        if (slots == 1 && blenderRenderTaskDatabaseService.listAll().size() == 1) {
                            counter = 0;
                        }
                        if (counter > 119) {
                            LOG.debug("Informing server of idle slot(s)");
                            counter = 0;
                            if (slots == 1) {
                                sendIdleUpdate(computeType);
                            }
                            if (slots == 2) {
                                if (blenderRenderTaskDatabaseService.listAll().size() == 0) {
                                    sendIdleUpdate(computeType);
                                } else {
                                    for (BlenderRenderTask blenderRenderTask : blenderRenderTaskDatabaseService.listAll()) {
                                        if (blenderRenderTask.getComputeType().equals(ComputeType.CPU)) {
                                            sendIdleUpdate(ComputeType.GPU);

                                        } else {
                                            sendIdleUpdate(ComputeType.CPU);

                                        }
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
        } catch (Exception e) {
            LOG.error("Unknown Exception caught, catching and logging");
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));

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
    public void setBlenderRenderTaskDatabaseService(BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService) {
        this.blenderRenderTaskDatabaseService = blenderRenderTaskDatabaseService;
    }
}
