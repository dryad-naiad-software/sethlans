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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderQueueItem;
import com.dryadandnaiad.sethlans.repositories.BlenderRenderQueueRepository;
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

    @Override
    public List<BlenderRenderQueueItem> listAll() {
        List<BlenderRenderQueueItem> blenderRenderQueueItems = new ArrayList<>();
        blenderRenderQueueRepository.findAll().forEach(blenderRenderQueueItems::add);
        return blenderRenderQueueItems;
    }

    @Override
    public BlenderRenderQueueItem getById(Integer id) {
        return blenderRenderQueueRepository.findOne(id);
    }

    @Override
    public BlenderRenderQueueItem saveOrUpdate(BlenderRenderQueueItem domainObject) {
        return blenderRenderQueueRepository.save(domainObject);
    }

    @Override
    public void delete(Integer id) {
        BlenderRenderQueueItem blenderRenderQueueItem = blenderRenderQueueRepository.findOne(id);
        blenderRenderQueueRepository.delete(blenderRenderQueueItem);
    }

    @Override
    public List<BlenderRenderQueueItem> queueItemsByConnectionUUID(String connection_uuid) {
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = listAll();
        List<BlenderRenderQueueItem> sortedList = new ArrayList<>();
        for (BlenderRenderQueueItem blenderRenderQueueItem :
                blenderRenderQueueItemList) {
            if (blenderRenderQueueItem.getConnection_uuid().equals(connection_uuid)) {
                sortedList.add(blenderRenderQueueItem);
            }

        }
        return sortedList;
    }

    @Override
    public List<BlenderRenderQueueItem> queueItemsByProjectUUID(String project_uuid) {
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