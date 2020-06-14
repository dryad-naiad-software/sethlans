/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.models.forms.NodeForm;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * File created by Mario Estrella on 6/12/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/v1/management")
@Profile({"SERVER", "DUAL"})
public class AdminServerController {

    private final NodeRepository nodeRepository;

    public AdminServerController(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @GetMapping("/node_scan")
    public Set<Node> nodeScan() {
        return NetworkUtils.discoverNodesViaMulticast();
    }

    @GetMapping("/node_list")
    public Set<Node> nodesSet(@RequestBody List<NodeForm> nodes) {
        var nodeSet = new HashSet<Node>();
        for (NodeForm node : nodes) {
            var retrievedNode = NetworkUtils.getNodeViaJson(node.getIpAddress(), node.getNetworkPort());
            if (retrievedNode != null) {
                nodeSet.add(retrievedNode);
            }
        }
        return nodeSet;
    }

    @PostMapping("/add_nodes")
    public ResponseEntity<Void> addNodes(@RequestBody List<NodeForm> selectedNodes) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
