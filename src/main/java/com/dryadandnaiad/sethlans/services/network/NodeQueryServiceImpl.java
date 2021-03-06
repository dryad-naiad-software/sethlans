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

package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.queue.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 4/1/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NodeQueryServiceImpl implements NodeQueryService {
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private QueueService queueService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeQueryServiceImpl.class);

    @Override
    @Async
    public void start() {
        try {
            Thread.sleep(15000);
            int count = 0;
            while (true) {
                try {
                    if (sethlansNodeDatabaseService.tableSize() > 0) {
                        Thread.sleep(2500);
                        for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
                            if (sethlansNode.isBenchmarkComplete()) {
                                boolean response = sethlansAPIConnectionService.queryNode("https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort()
                                        + "/api/info/node_keep_alive", "connection_uuid=" + sethlansNode.getConnectionUUID());
                                if (!response && sethlansNode.isActive()) {
                                    LOG.warn(sethlansNode.getHostname() + " is down.");
                                    queueService.nodeStatusUpdateItem(sethlansNode.getConnectionUUID(), false);
                                } else if (response && !sethlansNode.isDisabled() && !sethlansNode.isActive()) {
                                    queueService.nodeStatusUpdateItem(sethlansNode.getConnectionUUID(), true);
                                    LOG.info(sethlansNode.getHostname() + " is back online.");
                                }
                            }
                        }
                        count++;
                        if (count == 10) {
                            LOG.debug("One node heartbeat sent every 2.5 seconds.  Sent 10 heartbeat requests.");
                            count = 0;
                        }
                    } else {
                        Thread.sleep(30000);
                    }

                } catch (InterruptedException e) {
                    LOG.debug("Stopping Node Query Service");
                    break;
                }
            }
        } catch (InterruptedException e) {
            LOG.debug("Stopping Node Query Service");
        }


    }

    @Autowired
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }
}
