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

package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.google.common.base.Throwables;
import org.hibernate.StaleStateException;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        return new ArrayList<>(nodeRepository.findAll());
    }

    @Override
    public long tableSize() {
        return nodeRepository.count();
    }

    @Override
    public SethlansNode getById(Long id) {
        return nodeRepository.findOne(id);
    }

    @Override
    public SethlansNode saveOrUpdate(SethlansNode domainObject) {
        try {
            return nodeRepository.save(domainObject);
        } catch (ObjectOptimisticLockingFailureException | OptimisticEntityLockException | StaleStateException e) {
            LOG.error("Was unable to save node update.");
            LOG.error(Throwables.getStackTraceAsString(e));
            return domainObject;
        }
    }

    @Override
    public void delete(Long id) {
        SethlansNode sethlansNode = nodeRepository.findOne(id);
        String connectionURL = "https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() + "/api/nodeactivate/removal";
        String params = "connection_uuid=" + sethlansNode.getConnectionUUID();
        sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
        nodeRepository.delete(sethlansNode);
    }

    @Override
    public SethlansNode getByConnectionUUID(String uuid) {
        return nodeRepository.findSethlansNodeByConnectionUUID(uuid);
    }

    @Override
    public List<SethlansNode> activeNodeswithFreeSlots() {
        List<SethlansNode> nodes = activeNodeList();
        List<SethlansNode> nodesNotRendering = new ArrayList<>();
//        for (SethlansNode node : nodes) {
//            if (node.getAvailableRenderingSlots() > 0) {
//                nodesNotRendering.add(node);
//            }
//        }
        return nodesNotRendering;
    }

    @Override
    public boolean activeNodes() {
        return nodeRepository.existsSethlansNodesByActiveIsTrueAndBenchmarkCompleteIsTrueAndDisabledIsFalse();
    }

    @Override
    public List<SethlansNode> activeNonComboNodes() {
        List<SethlansNode> activeNodes = new ArrayList<>();
        for (SethlansNode node : activeNodeList()) {
            if (!node.isCombined()) {
                activeNodes.add(node);
            }
        }
        return activeNodes;
    }

    @Override
    public List<SethlansNode> activeNodeList() {
        return nodeRepository.findSethlansNodesByActiveIsTrueAndBenchmarkCompleteIsTrueAndDisabledIsFalse();
    }

    @Override
    public List<SethlansNode> activeCPUNodes() {
        return nodeRepository.findSethlansNodesByActiveIsTrueAndBenchmarkCompleteIsTrueAndDisabledIsFalseAndComputeTypeEquals(ComputeType.CPU);
    }

    @Override
    public List<SethlansNode> activeGPUNodes() {
        return nodeRepository.findSethlansNodesByActiveIsTrueAndBenchmarkCompleteIsTrueAndDisabledIsFalseAndComputeTypeEquals(ComputeType.GPU);
    }

    @Override
    public List<SethlansNode> activeCPUGPUNodes() {
        return nodeRepository.findSethlansNodesByActiveIsTrueAndBenchmarkCompleteIsTrueAndDisabledIsFalseAndComputeTypeEquals(ComputeType.CPU_GPU);
    }

    @Override
    public List<SethlansNode> activeNodesWithNoFreeSlots() {
        List<SethlansNode> nodes = activeNodeList();
        List<SethlansNode> nodesRendering = new ArrayList<>();
//        for (SethlansNode node : nodes) {
//            if (node.getAvailableRenderingSlots() == 0) {
//                nodesRendering.add(node);
//            }
//        }
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
                LOG.error(sethlansNode.getHostname() + " is already in the database.");
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

    @Override
    public void deleteByConnectionUUID(String connection_uuid) {
        SethlansNode sethlansNode = getByConnectionUUID(connection_uuid);
        nodeRepository.delete(sethlansNode);
    }

    @Override
    public List<SethlansNode> inactiveNodeList() {
        return nodeRepository.findSethlansNodesByActiveIsFalseOrBenchmarkCompleteIsFalse();
    }

    @Override
    public List<SethlansNode> disabledNodeList() {
        return nodeRepository.findSethlansNodesByDisabledIsTrue();
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
