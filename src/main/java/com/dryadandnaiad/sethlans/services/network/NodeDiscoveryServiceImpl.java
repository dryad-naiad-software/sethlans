package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Override
    public SethlansNode discoverMulticastNodes() {
        return null;
    }

    @Override
    public SethlansNode discoverUnicastNode(String ip, String port) {
        Gson gson = new Gson();
        return gson.fromJson(getRawDataService.getNodeResult("https://" + ip + ":" + port + "/nodeinfo"), SethlansNode.class);
    }

    @Autowired
    public void setGetRawDataService(GetRawDataService getRawDataService) {
        this.getRawDataService = getRawDataService;
    }
}
