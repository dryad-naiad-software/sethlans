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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */

@RestController
@Profile({"SERVER", "DUAL", "SETUP"})
@RequestMapping("/api/info")
public class ServerInfoController {
    @Value("${sethlans.configDir}")
    private String configDir;

    private SethlansNodeDatabaseService sethlansNodeDatabaseService;


    @GetMapping(value = {"/total_nodes"})
    public int getTotalNodes() {
        return sethlansNodeDatabaseService.listAll().size();
    }

    @GetMapping(value = {"/inactive_nodes"})
    public int getInactiveNodes() {
        return sethlansNodeDatabaseService.inactiveNodeList().size();
    }

    @GetMapping(value = {"/disabled_nodes"})
    public int getDisabledNodes() {
        return sethlansNodeDatabaseService.disabledNodeList().size();
    }

    @GetMapping(value = {"/active_nodes"})
    public int getActiveNodes() {
        return sethlansNodeDatabaseService.activeNodeList().size();
    }

    @GetMapping(value = {"/active_nodes_cpu"})
    public int getActiveCPUNodes() {
        return sethlansNodeDatabaseService.activeCPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_gpu"})
    public int getActiveGPUNodes() {
        return sethlansNodeDatabaseService.activeGPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_cpu_gpu"})
    public int getActiveCPUGPUNodes() {
        return sethlansNodeDatabaseService.activeCPUGPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_value_array"})
    public List<Integer> getNumberOfActiveNodesArray() {
        List<Integer> numberOfActiveNodesArray = new ArrayList<>();
        numberOfActiveNodesArray.add(getActiveCPUNodes());
        numberOfActiveNodesArray.add(getActiveGPUNodes());
        numberOfActiveNodesArray.add(getActiveCPUGPUNodes());
        return numberOfActiveNodesArray;
    }

    @GetMapping(value = {"/server_total_slots"})
    public int getTotalSlots() {
        int totalSlotsCount = 0;
        for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
            if (sethlansNode.isActive() && !sethlansNode.isDisabled() && sethlansNode.isBenchmarkComplete()) {
                totalSlotsCount = totalSlotsCount + sethlansNode.getTotalRenderingSlots();
            }
        }
        return totalSlotsCount;
    }

    @GetMapping(value = {"/server_used_space"})
    public Long getClientUsedSpace() {
        return getClientTotalSpace() - getClientFreeSpace();
    }


    @GetMapping(value = {"/server_free_space"})
    public Long getClientFreeSpace() {
        return new File(SethlansUtils.getProperty(SethlansConfigKeys.PROJECT_DIR.toString())).getFreeSpace() / 1024 / 1024 / 1024;
    }

    @GetMapping(value = {"/server_total_space"})
    public Long getClientTotalSpace() {
        return new File(SethlansUtils.getProperty(SethlansConfigKeys.PROJECT_DIR.toString())).getTotalSpace() / 1024 / 1024 / 1024;

    }


    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }
}
