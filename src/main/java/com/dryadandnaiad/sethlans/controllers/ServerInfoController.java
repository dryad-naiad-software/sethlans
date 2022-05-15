/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.models.query.ServerDashboard;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * File created by Mario Estrella on 5/14/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/info")
@Profile({"SERVER", "DUAL"})
public class ServerInfoController {
    private final NodeRepository nodeRepository;

    public ServerInfoController(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @GetMapping("/server_dashboard")
    public ServerDashboard getServerDashboard() {
        var systemInfo = QueryUtils.getCurrentSystemInfo();

        var activeNodes = this.nodeRepository.countNodesByActiveIsTrue();
        var totalNodes = this.nodeRepository.count();
        var disabledNodes = totalNodes - activeNodes;
        var nodes = nodeRepository.findNodesByBenchmarkCompleteTrueAndActiveTrue();
        var slots = 0;
        var nodeDistribution = new ArrayList<Integer>() {
            {
                add(0);
                add(0);
                add(0);
            }
        };


        for (Node node : nodes) {
            slots += node.getTotalRenderingSlots();
            var value = 0;
            switch (node.getNodeType()) {
                case CPU_GPU -> {
                    value = nodeDistribution.get(0);
                    nodeDistribution.set(0, value + 1);
                }
                case GPU -> {
                    value = nodeDistribution.get(1);
                    nodeDistribution.set(1, value + 1);
                }
                case CPU -> {
                    value = nodeDistribution.get(2);
                    nodeDistribution.set(2, value + 1);
                }
            }
        }

        return ServerDashboard.builder()
                .activeNodes((int) activeNodes)
                .totalNodes((int) totalNodes)
                .cpuName(systemInfo.getCpu().getName())
                .totalMemory(systemInfo.getCpu().getTotalMemory())
                .freeSpace(QueryUtils.getClientFreeSpace())
                .totalSpace(QueryUtils.getClientTotalSpace())
                .usedSpace(QueryUtils.getClientUsedSpace())
                .disabledNodes((int) disabledNodes)
                .nodeDistribution(nodeDistribution)
                .totalSlots(slots)
                .build();
    }
}
