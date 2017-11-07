package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;

import java.util.List;

/**
 * Created Mario Estrella on 10/29/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface NodeDiscoveryService {

    List<SethlansNode> discoverMulticastNodes();

    boolean isListComplete();

    void multicastDiscovery();

    void resetNodeList();

    SethlansNode discoverUnicastNode(String ip, String port);

}
