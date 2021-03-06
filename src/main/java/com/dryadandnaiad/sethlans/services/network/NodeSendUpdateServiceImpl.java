/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.systray.SystrayService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
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
    private BenchmarkTaskDatabaseService benchmarkTaskDatabaseService;
    private SystrayService systrayService;
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
            NodeInfo nodeInfo = SethlansNodeUtils.getNodeInfo();
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
                    if (sethlansServerDatabaseService.tableSize() == 0) {
                        Thread.sleep(5000);
                    } else {
                        Thread.sleep(1000);
                    }

                    if (sethlansServerDatabaseService.listActive().size() > 0 && benchmarkTaskDatabaseService.allBenchmarksComplete()) {
                        if (renderTaskDatabaseService.tableSize() > 0) {
                            if (!GraphicsEnvironment.isHeadless()) {
                                systrayService.nodeState(true);
                            }
                            counter = 0;
                        }
                        if (renderTaskDatabaseService.tableSize() == 0) {
                            if (!GraphicsEnvironment.isHeadless()) {
                                systrayService.nodeState(false);
                            }
                            counter++;
                        }
                        if (counter % 60 == 0 && counter > 59) {
                            LOG.debug("Node idle for " + (counter / 60) + " minutes");
                        }
                        if (counter > 120) {
                            LOG.info("Informing server of idle slot(s)");
                            counter = 0;
                            if (slots == 1) {
                                sendIdleUpdate(computeType);
                            }
                            if (slots > 1) {
                                if (renderTaskDatabaseService.tableSize() == 0) {
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

                } catch (InterruptedException e) {
                    LOG.debug("Shutting Down Node Idle Notification Service");
                    break;
                }


            }
        } catch (InterruptedException e) {
            LOG.debug("Shutting Down Node Idle Notification Service");
        }


    }

    private void sendIdleUpdate(ComputeType computeType) {
        //TODO get which GPU is being used if GPU mode
        for (SethlansServer sethlansServer : sethlansServerDatabaseService.listAll()) {
            String url = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/update/node_idle_notification";
            String param = "connection_uuid=" + sethlansServer.getConnectionUUID() + "&compute_type=" + computeType;
            sethlansAPIConnectionService.sendToRemotePOST(url, param);
            String cacheDir = SethlansConfigUtils.getProperty(SethlansConfigKeys.CACHE_DIR);
            String blendFileCache = SethlansConfigUtils.getProperty(SethlansConfigKeys.BLEND_FILE_CACHE_DIR);
            File cacheDirToClean = new File(cacheDir);
            File blendFileDirToClean = new File(blendFileCache);
            try {
                if (cacheDirToClean.exists()) {
                    FileUtils.cleanDirectory(cacheDirToClean);
                }
                if (blendFileDirToClean.exists()) {
                    FileUtils.cleanDirectory(blendFileDirToClean);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage() + Throwables.getStackTraceAsString(e));
            }
        }
    }




    private void sendRequest() {
        List<SethlansServer> sethlansServers = sethlansServerDatabaseService.listAll();
        if (!sethlansServers.isEmpty()) {
            for (SethlansServer sethlansServer : sethlansServers) {
                if (sethlansServer.isNodeUpdated()) {
                    LOG.debug("Sending node status update request to " + sethlansServer.getHostname());
                    String url = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/update/node_status_update/";
                    String param = "connection_uuid=" + sethlansServer.getConnectionUUID();
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
    public void setBenchmarkTaskDatabaseService(BenchmarkTaskDatabaseService benchmarkTaskDatabaseService) {
        this.benchmarkTaskDatabaseService = benchmarkTaskDatabaseService;
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

    @Autowired
    public void setSystrayService(SystrayService systrayService) {
        this.systrayService = systrayService;
    }
}
