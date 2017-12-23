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

package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.services.blender.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.blender.BlenderDownloadService;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created Mario Estrella on 12/5/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NodeActivationServiceImpl implements NodeActivationService, ApplicationEventPublisherAware {
    private static final Logger LOG = LoggerFactory.getLogger(NodeActivationServiceImpl.class);
    private ApplicationEventPublisher applicationEventPublisher;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private BlenderDownloadService blenderDownloadService;
    private BlenderBenchmarkService blenderBenchmarkService;


    @Override
    @Async
    public void sendActivationRequest(SethlansNode sethlansNode, SethlansServer sethlansServer) {
        LOG.debug("Sending Activation Request to Node");
        String ip = sethlansNode.getIpAddress();
        String port = sethlansNode.getNetworkPort();
        String activateURL = "https://" + ip + ":" + port + "/api/nodeactivate/request";
        String params = "serverhostname=" + sethlansServer.getHostname() + "&ipAddress=" + sethlansServer.getIpAddress()
                + "&port=" + sethlansServer.getNetworkPort() + "&connection_uuid=" + sethlansNode.getConnection_uuid();
        if (sethlansAPIConnectionService.sendToRemotePOST(activateURL, params)) {
            addBlenderBinary(sethlansNode.getSethlansNodeOS().toString());
            LOG.debug("Node activation sent, downloading blender zip files");
            blenderDownloadService.downloadRequestedBlenderFilesAsync();
        }


    }

    @Override
    @Async
    public void sendActivationResponse(SethlansServer sethlansServer, SethlansNode sethlansNode) {
        LOG.debug("Sending Activation Response to Server");
        String ip = sethlansServer.getIpAddress();
        String port = sethlansServer.getNetworkPort();
        String responseURL = "https://" + ip + ":" + port + "/api/nodeactivate/response";
        String params = "nodehostname=" + sethlansNode.getHostname() + "&ipAddress=" + sethlansNode.getIpAddress()
                + "&port=" + sethlansNode.getNetworkPort() + "&connection_uuid=" + sethlansServer.getConnection_uuid();
        if (sethlansAPIConnectionService.sendToRemotePOST(responseURL, params)) {
            this.applicationEventPublisher.publishEvent(new SethlansEvent(this, sethlansServer.getHostname(), false));

        }
    }

    @Override
    @Async
    public void sendResponseAcknowledgement(SethlansNode sethlansNode, String connection_uuid) {
        LOG.debug("Sending Response Acknowledgement to Node");
        String ip = sethlansNode.getIpAddress();
        String port = sethlansNode.getNetworkPort();
        String acknowledgeURL = "https://" + ip + ":" + port + "/api/nodeactivate/acknowledge";
        String params = "connection_uuid=" + sethlansNode.getConnection_uuid();
        boolean pendingDownloads = true;
        if (sethlansAPIConnectionService.sendToRemotePOST(acknowledgeURL, params)) {
            while (pendingDownloads) {
                pendingDownloads = false;
                List<BlenderBinary> blenderBinaries = blenderBinaryDatabaseService.listAll();
                for (BlenderBinary blenderBinary : blenderBinaries) {
                    if (!blenderBinary.isDownloaded()) {
                        LOG.debug("Blender binary download is in progress, holding off on sending benchmark request");
                        try {
                            pendingDownloads = true;
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
            try {
                Thread.sleep(5000);
                blenderBenchmarkService.sendBenchmarktoNode(sethlansNode);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private void addBlenderBinary(String serverOS) {
        LOG.debug("Checking to see if blender binaries need to be downloaded for new node.");
        // TODO Some kind of logic error here is causing multiple downloads.  To address tomorrow
        List<BlenderBinary> blenderBinaries = blenderBinaryDatabaseService.listAll();
        Set<String> versions = new HashSet<>();
        for (BlenderBinary blenderBinary : blenderBinaries) {
            versions.add(blenderBinary.getBlenderVersion());
            LOG.debug("Adding blender binary version " + blenderBinary.getBlenderVersion() + " to set for verification");
            for (String version : versions) {
                if (blenderBinary.getBlenderBinaryOS().equals(serverOS) && blenderBinary.getBlenderVersion().equals(version)) {
                    LOG.debug("Blender Binaries already present.");
                } else {
                    BlenderBinary newBlenderBinary = new BlenderBinary();
                    newBlenderBinary.setDownloaded(false);
                    newBlenderBinary.setBlenderBinaryOS(serverOS);
                    newBlenderBinary.setBlenderVersion(version);
                    LOG.debug("Adding " + newBlenderBinary.toString() + " to database.");
                    blenderBinaryDatabaseService.saveOrUpdate(newBlenderBinary);
                }
            }

        }
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setBlenderDownloadService(BlenderDownloadService blenderDownloadService) {
        this.blenderDownloadService = blenderDownloadService;
    }

    @Autowired
    public void setBlenderBenchmarkService(BlenderBenchmarkService blenderBenchmarkService) {
        this.blenderBenchmarkService = blenderBenchmarkService;
    }
}
