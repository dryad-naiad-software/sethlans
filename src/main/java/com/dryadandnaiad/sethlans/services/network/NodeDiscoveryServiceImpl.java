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
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 11/1/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NodeDiscoveryServiceImpl implements NodeDiscoveryService {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscoveryServiceImpl.class);

    private GetRawDataService getRawDataService;
    private MulticastReceiverService multicastReceiverService;
    private List<SethlansNode> sethlansNodeList;
    private boolean listComplete = false;
    private boolean scanInProgress = false;


    @Override
    public List<SethlansNode> discoverMulticastNodes() {
        if (listComplete) {
            return sethlansNodeList;
        } else {
            return null;
        }

    }

    @Async
    public void multicastDiscovery() {
            if (!scanInProgress && !listComplete) {
                scanInProgress = true;
                LOG.info("Starting Discovery");
                Set<String> nodeList = multicastReceiverService.currentSethlansClients();
                if (nodeList != null) {
                    sethlansNodeList = new ArrayList<>();
                    for (String node : nodeList) {
                        LOG.debug(node);
                        String[] split = node.split(":");
                        String ip = split[0];
                        String port = split[1];
                        SethlansNode newSethlansNode;
                        try {
                            newSethlansNode = discoverUnicastNode(ip, port);
                            if (newSethlansNode != null) {
                                LOG.debug(newSethlansNode.toString());
                                sethlansNodeList.add(newSethlansNode);
                            }
                        } catch (NullPointerException e) {
                            LOG.debug(Throwables.getStackTraceAsString(e));
                        }

                    }
                    listComplete = true;
                    scanInProgress = false;
                }
            }
    }

    public void resetNodeList(){
        sethlansNodeList = null;
        listComplete = false;
    }


    @Override
    public SethlansNode discoverUnicastNode(String ip, String port) {
        LOG.info("Searching for Sethlans Node at " + ip + ":" + port);
        String accessKey = getProperty(SethlansConfigKeys.ACCESS_KEY);
        Gson gson = new Gson();
        SethlansNode sethlansNode = null;
        try {
            sethlansNode = gson.fromJson(getRawDataService.getNodeResult("https://" + ip + ":" + port + "/api/info/nodeinfo" + "/?access_key=" + accessKey), SethlansNode.class);
            sethlansNode.setPendingActivation(true);
            sethlansNode.setDisabled(false);
            sethlansNode.setActive(false);
            switch (sethlansNode.getComputeType()) {
                case CPU:
                    sethlansNode.setTotalRenderingSlots(1);
                    break;
                case GPU:
                    sethlansNode.setSelectedGPURatings(new ArrayList<>());
                    if (sethlansNode.isCombined()) {
                        sethlansNode.setTotalRenderingSlots(1);
                    } else {
                        sethlansNode.setTotalRenderingSlots(sethlansNode.getSelectedGPUs().size());
                    }
                    break;
                case CPU_GPU:
                    sethlansNode.setSelectedGPURatings(new ArrayList<>());
                    if (sethlansNode.isCombined()) {
                        sethlansNode.setTotalRenderingSlots(2);
                    } else {
                        sethlansNode.setTotalRenderingSlots(sethlansNode.getSelectedGPUs().size() + 1);
                    }
                    break;
            }
            sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
            sethlansNode.setCpuSlotInUse(false);
            sethlansNode.setAllGPUSlotInUse(false);
            sethlansNode.setConnectionUUID(UUID.randomUUID().toString());

        } catch (NullPointerException e) {
            LOG.error("Unable to read JSON data from " + ip + ":" + port);
        }
        return sethlansNode;
    }

    @Autowired
    public void setGetRawDataService(GetRawDataService getRawDataService) {
        this.getRawDataService = getRawDataService;
    }

    @Autowired
    public void setMulticastReceiverService(MulticastReceiverService multicastReceiverService) {
        this.multicastReceiverService = multicastReceiverService;
    }

    public boolean isListComplete() {
        return listComplete;
    }
}
