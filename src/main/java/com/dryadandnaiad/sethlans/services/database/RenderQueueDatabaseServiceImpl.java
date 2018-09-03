/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.repositories.RenderQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class RenderQueueDatabaseServiceImpl implements RenderQueueDatabaseService {
    private RenderQueueRepository renderQueueRepository;

    private static final Logger LOG = LoggerFactory.getLogger(RenderQueueDatabaseServiceImpl.class);

    @Override
    public long tableSize() {
        return renderQueueRepository.count();
    }

    @Override
    public List<RenderQueueItem> listAll() {
        return new ArrayList<>(renderQueueRepository.findAll());
    }

    @Override
    public List<RenderQueueItem> listPendingRender() {
        return renderQueueRepository.findRenderQueueItemsByCompleteIsFalseAndRenderingIsFalseAndPausedIsFalse();
    }

    @Override
    public boolean checkExistingProjectIndex(String projectUUID, int index) {
        for (RenderQueueItem renderQueueItem : listQueueItemsByProjectUUID(projectUUID)) {
            if (renderQueueItem.getProjectIndex() == index) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<RenderQueueItem> listPendingRenderWithNodeAssigned() {
        return renderQueueRepository.findRenderQueueItemsByCompleteIsFalseAndRenderingIsFalseAndPausedIsFalseAndConnectionUUIDIsNotNull();
    }


    @Override
    public RenderQueueItem getById(Long id) {
        return renderQueueRepository.findOne(id);
    }

    @Override
    public RenderQueueItem getByQueueUUID(String queueUUID) {
        return renderQueueRepository.findRenderQueueItemByQueueItemUUID(queueUUID);
    }

    @Override
    public RenderQueueItem saveOrUpdate(RenderQueueItem domainObject) {
        return renderQueueRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        RenderQueueItem renderQueueItem = renderQueueRepository.findOne(id);
        renderQueueRepository.delete(renderQueueItem);
    }

    @Override
    public void deleteAllByProject(String project_uuid) {
        renderQueueRepository.deleteRenderQueueItemsByProjectUUID(project_uuid);
    }

    @Override
    public void delete(RenderQueueItem renderQueueItem) {
        renderQueueRepository.delete(renderQueueItem);
    }

    @Override
    public List<RenderQueueItem> listQueueItemsByConnectionUUID(String connection_uuid) {
        return renderQueueRepository.findRenderQueueItemsByConnectionUUID(connection_uuid);
    }

    @Override
    public List<RenderQueueItem> listQueueItemsByProjectUUID(String project_uuid) {
        return renderQueueRepository.findRenderQueueItemsByProjectUUID(project_uuid);
    }

    @Autowired
    public void setRenderQueueRepository(RenderQueueRepository renderQueueRepository) {
        this.renderQueueRepository = renderQueueRepository;
    }


}
