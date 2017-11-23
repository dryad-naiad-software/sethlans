/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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
