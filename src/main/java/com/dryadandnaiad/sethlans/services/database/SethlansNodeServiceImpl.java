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
public class SethlansNodeServiceImpl implements SethlansNodeService {

    private NodeRepository nodeRepository;

    @Override
    public List<?> listAll() {
        List<SethlansNode> sethlansNodes = new ArrayList<>();
        nodeRepository.findAll().forEach(sethlansNodes :: add);
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

    @Autowired
    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }
}
