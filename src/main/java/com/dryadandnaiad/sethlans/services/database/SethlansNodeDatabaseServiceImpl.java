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

package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 11/1/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansNodeDatabaseServiceImpl implements SethlansNodeDatabaseService {

    private NodeRepository nodeRepository;

    @Override
    public List<SethlansNode> listAll() {
        List<SethlansNode> sethlansNodes = new ArrayList<>();
        nodeRepository.findAll().forEach(sethlansNodes::add);
        return sethlansNodes;
    }

    @Override
    public SethlansNode getById(Integer id) {
        return nodeRepository.findOne(id);
    }

    @Override
    public SethlansNode saveOrUpdate(SethlansNode domainObject) {
        return nodeRepository.save(domainObject);
    }

    @Override
    public void delete(Integer id) {
        SethlansNode sethlansNode = nodeRepository.findOne(id);
        nodeRepository.delete(sethlansNode);
    }

    @Override
    public SethlansNode getByConnectionUUID(String uuid) {
        List<SethlansNode> sethlansNodes = listAll();
        for (SethlansNode sethlansNode : sethlansNodes) {
            if (sethlansNode.getConnection_uuid().equals(uuid)) {
                return sethlansNode;
            }
        }
        return null;
    }

    @Autowired
    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }
}
