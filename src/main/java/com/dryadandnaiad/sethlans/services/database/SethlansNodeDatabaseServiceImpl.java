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

package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 11/1/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansNodeDatabaseServiceImpl implements SethlansNodeDatabaseService {

    private NodeRepository nodeRepository;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private static final Logger LOG = LoggerFactory.getLogger(SethlansNodeDatabaseServiceImpl.class);

    @Override
    public List<SethlansNode> listAll() {
        List<SethlansNode> sethlansNodes = new ArrayList<>();
        nodeRepository.findAll().forEach(sethlansNodes::add);
        return sethlansNodes;
    }

    @Override
    public SethlansNode getById(Long id) {
        return nodeRepository.findOne(id);
    }

    @Override
    public SethlansNode saveOrUpdate(SethlansNode domainObject) {
        return nodeRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        SethlansNode sethlansNode = nodeRepository.findOne(id);
        String connectionURL = "https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() + "/api/nodeactivate/removal";
        String params = "connection_uuid=" + sethlansNode.getConnection_uuid();
        sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
        nodeRepository.delete(sethlansNode);
    }

    @Override
    public SethlansNode getByConnectionUUID(String uuid) {
        List<SethlansNode> sethlansNodes = listAll();
        for (SethlansNode sethlansNode : sethlansNodes) {
            if (sethlansNode.getConnection_uuid().equals(uuid)) {
                return sethlansNode;
            }
        }
        return null;
    }

    @Override
    public List<SethlansNode> activeNodesNotRendering() {
        List<SethlansNode> nodes = listAll();
        List<SethlansNode> nodesNotRendering = new ArrayList<>();
        for (SethlansNode node : nodes) {
            if (!node.isRendering() && node.isActive() && node.isBenchmarkComplete()) {
                nodesNotRendering.add(node);

            }
        }
        return nodesNotRendering;
    }

    @Override
    public List<SethlansNode> activeNodesRendering() {
        List<SethlansNode> nodes = listAll();
        List<SethlansNode> nodesRendering = new ArrayList<>();
        for (SethlansNode node : nodes) {
            if (node.isRendering() && node.isActive() && node.isBenchmarkComplete()) {
                nodesRendering.add(node);

            }
        }
        return nodesRendering;
    }

    @Override
    public boolean checkForDuplicatesAndSave(SethlansNode sethlansNode) {
        List<SethlansNode> storedNodes = listAll();
        List<SethlansNode> matchedNodes = new ArrayList<>();
        for (SethlansNode storedNode : storedNodes) {
            if (storedNode.getIpAddress().equals(sethlansNode.getIpAddress()) &&
                    storedNode.getNetworkPort().equals(sethlansNode.getNetworkPort()) &&
                    storedNode.getComputeType().equals(sethlansNode.getComputeType()) &&
                    storedNode.getSethlansNodeOS().equals(sethlansNode.getSethlansNodeOS())) {
                LOG.debug(sethlansNode.getHostname() + " is already in the database.");
                matchedNodes.add(storedNode);
            }
        }
        if (matchedNodes.size() == 0) {
            nodeRepository.save(sethlansNode);
            return true;
        } else {
            return false;
        }
    }

    @Autowired
    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }
}
