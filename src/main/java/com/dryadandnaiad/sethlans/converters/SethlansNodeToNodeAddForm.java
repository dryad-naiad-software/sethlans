package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.commands.NodeAddForm;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Created Mario Estrella on 11/5/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class SethlansNodeToNodeAddForm implements Converter<SethlansNode, NodeAddForm> {
    @Override
    public NodeAddForm convert(SethlansNode sethlansNode) {
        NodeAddForm nodeAddForm = new NodeAddForm();
        nodeAddForm.setVersion(sethlansNode.getVersion());
        nodeAddForm.setId(sethlansNode.getId());
        nodeAddForm.setIpAddress(sethlansNode.getIpAddress());
        nodeAddForm.setPort(sethlansNode.getNetworkPort());
        return nodeAddForm;
    }
}
