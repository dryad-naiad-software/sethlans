package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
@RequestMapping("/api/setup")
public class ServerSetupController {
    private NodeActivationService nodeActivationService;
    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(ServerSetupController.class);


    @GetMapping("/auto_acknowledge/{connection_id}")
    public void autoAcknowledgeNode(@PathVariable String connection_id) {
        SethlansServer sethlansServer = SethlansUtils.getCurrentServerInfo();
        sethlansServer.setConnection_uuid(connection_id);
        nodeActivationService.sendActivationResponse(sethlansServer, sethlansNodeDatabaseService.getByConnectionUUID(connection_id), true);
    }

    @PostMapping("/multi_auto_acknowledge")
    public void multiAutoAcknowledge(@RequestBody String[] connectionIdArray) {
        for (String connectionId : connectionIdArray) {
            autoAcknowledgeNode(connectionId);
        }
    }


    @PostMapping("/multi_node_add")
    public List<String> addMultiNodes(@RequestBody String[] nodeIPArray) {
        List<String> connectionIds = new ArrayList<>();
        for (String node : nodeIPArray) {
            String[] nodeInfo = StringUtils.split(node, ",");
            connectionIds.add(addNode(nodeInfo[0], nodeInfo[1]));
        }
        return connectionIds;
    }

    @GetMapping("/node_add")
    public String addNode(@RequestParam String ip, @RequestParam String port) {
        SethlansNode sethlansNode = nodeDiscoveryService.discoverUnicastNode(ip, port);
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        if (!sethlansNodeList.isEmpty()) {
            LOG.debug("Nodes found in database, starting comparison.");
            if (sethlansNodeDatabaseService.checkForDuplicatesAndSave(sethlansNode)) {
                if (sethlansNode.isPendingActivation()) {
                    nodeActivationService.sendActivationRequest(sethlansNode, SethlansUtils.getCurrentServerInfo(), true);
                    return sethlansNode.getConnection_uuid();

                }
            }
        } else {
            LOG.debug("No nodes present in database.");
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            LOG.debug("Added: " + sethlansNode.getHostname() + " to database.");
            if (sethlansNode.isPendingActivation()) {
                nodeActivationService.sendActivationRequest(sethlansNode, SethlansUtils.getCurrentServerInfo(), true);
                return sethlansNode.getConnection_uuid();
            }
        }

        return null;
    }

    @GetMapping("/node_delete/{id}")
    public boolean deleteNode(@PathVariable Long id) {
        sethlansNodeDatabaseService.delete(id);
        return true;
    }

    @GetMapping("/node_edit/{id}")
    public boolean updateNode(@PathVariable Long id, @RequestParam String ip, @RequestParam String port) {
        SethlansNode sethlansNodeToEdit = sethlansNodeDatabaseService.getById(id);
        SethlansNode newNode = nodeDiscoveryService.discoverUnicastNode(ip, port);
        newNode.setId(sethlansNodeToEdit.getId());
        newNode.setVersion(sethlansNodeToEdit.getVersion());
        newNode.setDateCreated(sethlansNodeToEdit.getDateCreated());
        newNode.setLastUpdated(sethlansNodeToEdit.getLastUpdated());
        sethlansNodeDatabaseService.saveOrUpdate(newNode);
        return true;
    }

    @Autowired
    public void setNodeActivationService(NodeActivationService nodeActivationService) {
        this.nodeActivationService = nodeActivationService;
    }

    @Autowired
    public void setNodeDiscoveryService(NodeDiscoveryService nodeDiscoveryService) {
        this.nodeDiscoveryService = nodeDiscoveryService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

}
