package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        if(!scanInProgress && !listComplete) {
            scanInProgress = true;
            LOG.debug("Starting Discovery");
            Set<String> nodeList = multicastReceiverService.currentSethlansClients();
            if (nodeList != null) {
                sethlansNodeList = new ArrayList<>();
                for (String node : nodeList) {
                    LOG.debug(node);
                    String[] split = node.split(":");
                    String ip = split[0];
                    String port = split[1];
                    sethlansNodeList.add(discoverUnicastNode(ip, port));
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
        Gson gson = new Gson();
        SethlansNode sethlansNode = gson.fromJson(getRawDataService.getNodeResult("https://" + ip + ":" + port + "/nodeinfo"), SethlansNode.class);
        sethlansNode.setActive(true);
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
