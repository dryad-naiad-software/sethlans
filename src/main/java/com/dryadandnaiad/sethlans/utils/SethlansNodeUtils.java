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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 8/23/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansNodeUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansNodeUtils.class);

    public static NodeInfo getNodeInfo() {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.populateNodeInfo();
        ComputeType computeType = ComputeType.valueOf(SethlansConfigUtils.getProperty(SethlansConfigKeys.COMPUTE_METHOD, SethlansConfigUtils.getConfigFile()));

        nodeInfo.setNetworkPort(SethlansQueryUtils.getPort());
        nodeInfo.setComputeType(computeType);

        if (computeType == ComputeType.CPU_GPU || computeType == ComputeType.CPU) {
            nodeInfo.setCpuinfo();
            nodeInfo.setSelectedCores(SethlansQueryUtils.getSelectedCores());
        }

        if (computeType == ComputeType.GPU || computeType == ComputeType.CPU_GPU) {
            List<String> deviceList = Arrays.asList(SethlansConfigUtils.getProperty(SethlansConfigKeys.GPU_DEVICE, SethlansConfigUtils.getConfigFile()).split(","));
            nodeInfo.setCpuinfo();
            nodeInfo.setSelectedDeviceID(deviceList);
            nodeInfo.setSelectedGPUs();
            nodeInfo.setCombined(Boolean.parseBoolean(SethlansConfigUtils.getProperty(SethlansConfigKeys.COMBINE_GPU, SethlansConfigUtils.getConfigFile())));

        }
        return nodeInfo;
    }

    public static boolean sortedNodeList(ComputeType computeType, List<SethlansNode> listToSort) {
        if (listToSort.size() > 0) {
            switch (computeType) {
                case CPU:
                    LOG.debug("Sorting by CPU");
                    listToSort.sort(Comparator.comparing(SethlansNode::getCpuRating));
                    return true;
                case GPU:
                    LOG.debug("Sorting by GPU");
                    listToSort.sort(Comparator.comparing(SethlansNode::getCombinedGPURating));
                    return true;
                case CPU_GPU:
                    LOG.debug("Sorting by CPU_GPU");
                    listToSort.sort(Comparator.comparing(SethlansNode::getCombinedCPUGPURating));
                    return true;
            }
        }
        return false;
    }

    public static void cpuGPUNodeCheck(SetupNode setupNode) {
        if (setupNode.getTotalCores().equals(setupNode.getCores())) {
            setupNode.setCores(setupNode.getCores() - 1);
        }
    }

    public static void listofNodes(ComputeType computeType, List<SethlansNode> listToSort, SethlansNode sethlansNode) {
        switch (computeType) {
            case CPU:
                if (sethlansNode.getComputeType().equals(ComputeType.CPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    listToSort.add(sethlansNode);
                }
                break;
            case GPU:
                if (sethlansNode.getComputeType().equals(ComputeType.GPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    listToSort.add(sethlansNode);
                }
                break;
            case CPU_GPU:
                if (sethlansNode.getComputeType().equals(ComputeType.CPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.GPU)) {
                    listToSort.add(sethlansNode);
                }
                if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU)) {
                    listToSort.add(sethlansNode);
                }
        }
    }


    public static Integer getAvailableSlots(int slotsInUse) {
        int totalSlots = getTotalSlots();
        return totalSlots - slotsInUse;
    }

    public static Integer getTotalSlots() {
        boolean combined = Boolean.parseBoolean(getProperty(SethlansConfigKeys.COMBINE_GPU.toString()));
        NodeInfo nodeInfo = getNodeInfo();
        switch (nodeInfo.getComputeType()) {
            case CPU:
                return 1;
            case GPU:
                if (combined) {
                    return 1;
                } else {
                    return nodeInfo.getSelectedGPUs().size();
                }
            case CPU_GPU:
                if (combined) {
                    return 2;
                } else {
                    return nodeInfo.getSelectedGPUs().size() + 1;
                }
        }
        return 0;
    }

}
