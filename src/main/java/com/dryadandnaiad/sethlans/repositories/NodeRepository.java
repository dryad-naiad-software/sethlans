package com.dryadandnaiad.sethlans.repositories;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import org.springframework.data.repository.CrudRepository;

/**
 * Created Mario Estrella on 11/1/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface NodeRepository extends CrudRepository<SethlansNode, Integer> {
}
