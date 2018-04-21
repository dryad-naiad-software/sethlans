/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderQueueItem;
import com.dryadandnaiad.sethlans.repositories.BlenderRenderQueueRepository;
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
public class BlenderRenderQueueDatabaseServiceImpl implements BlenderRenderQueueDatabaseService {
    private BlenderRenderQueueRepository blenderRenderQueueRepository;

    private static final Logger LOG = LoggerFactory.getLogger(BlenderRenderQueueDatabaseServiceImpl.class);

    @Override
    public List<BlenderRenderQueueItem> listAll() {
        return new ArrayList<>(blenderRenderQueueRepository.findAll());
    }

    @Override
    public List<BlenderRenderQueueItem> listPendingRender() {
        List<BlenderRenderQueueItem> blenderRenderQueueItemsPending = new ArrayList<>();
        for (BlenderRenderQueueItem blenderRenderQueueItem : listAll()) {
            if (!blenderRenderQueueItem.isComplete() && !blenderRenderQueueItem.isRendering() && !blenderRenderQueueItem.isPaused()) {
                blenderRenderQueueItemsPending.add(blenderRenderQueueItem);
            }
        }
        return blenderRenderQueueItemsPending;
    }

    @Override
    public List<BlenderRenderQueueItem> listPendingRenderWithNodeAssigned() {
        List<BlenderRenderQueueItem> blenderRenderQueueItemsPending = new ArrayList<>();
        for (BlenderRenderQueueItem blenderRenderQueueItem : listAll()) {
            if (!blenderRenderQueueItem.isComplete() && !blenderRenderQueueItem.isRendering()
                    && !blenderRenderQueueItem.isPaused() && blenderRenderQueueItem.getConnection_uuid() != null) {
                blenderRenderQueueItemsPending.add(blenderRenderQueueItem);
            }
        }
        return blenderRenderQueueItemsPending;
    }


    @Override
    public BlenderRenderQueueItem getById(Long id) {
        return blenderRenderQueueRepository.findOne(id);
    }

    @Override
    public BlenderRenderQueueItem saveOrUpdate(BlenderRenderQueueItem domainObject) {
        return blenderRenderQueueRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        BlenderRenderQueueItem blenderRenderQueueItem = blenderRenderQueueRepository.findOne(id);
        blenderRenderQueueRepository.delete(blenderRenderQueueItem);
    }

    @Override
    public void deleteAllByProject(String project_uuid) {
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = listAll();
        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
            if (blenderRenderQueueItem.getProject_uuid().equals(project_uuid)) {
                blenderRenderQueueRepository.delete(blenderRenderQueueItem);
            }
        }

    }

    @Override
    public void delete(BlenderRenderQueueItem blenderRenderQueueItem) {
        blenderRenderQueueRepository.delete(blenderRenderQueueItem);
    }

    @Override
    public List<BlenderRenderQueueItem> listQueueItemsByConnectionUUID(String connection_uuid) {
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = listAll();
        List<BlenderRenderQueueItem> sortedList = new ArrayList<>();
        for (BlenderRenderQueueItem blenderRenderQueueItem :
                blenderRenderQueueItemList) {
            if (blenderRenderQueueItem.getConnection_uuid() != null && blenderRenderQueueItem.getConnection_uuid().equals(connection_uuid)) {
                sortedList.add(blenderRenderQueueItem);
            }


        }
        return sortedList;
    }

    @Override
    public List<BlenderRenderQueueItem> listQueueItemsByProjectUUID(String project_uuid) {
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = listAll();
        List<BlenderRenderQueueItem> sortedList = new ArrayList<>();
        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
            if (blenderRenderQueueItem.getProject_uuid().equals(project_uuid)) {
                sortedList.add(blenderRenderQueueItem);
            }
        }
        return sortedList;
    }

    @Autowired
    public void setBlenderRenderQueueRepository(BlenderRenderQueueRepository blenderRenderQueueRepository) {
        this.blenderRenderQueueRepository = blenderRenderQueueRepository;
    }


}
