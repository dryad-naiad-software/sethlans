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

import com.dryadandnaiad.sethlans.domains.database.queue.RenderTask;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class RenderTaskDatabaseServiceImpl implements RenderTaskDatabaseService {
    private RenderTaskRepository renderTaskRepository;
    private static final Logger LOG = LoggerFactory.getLogger(RenderTaskDatabaseServiceImpl.class);

    @Override
    public long tableSize() {
        return renderTaskRepository.count();
    }

    @Override
    public List<RenderTask> listAll() {
        return new ArrayList<>(renderTaskRepository.findAll());
    }

    @Override
    public List<String> deviceList() {
        List<String> deviceList = new ArrayList<>();
        for (RenderTask renderTask : listAll()) {
            deviceList.add(renderTask.getDeviceID());
        }
        return deviceList;
    }

    @Override
    public RenderTask getById(Long id) {
        return renderTaskRepository.findOne(id);
    }

    @Override
    public RenderTask saveOrUpdate(RenderTask domainObject) {
        return renderTaskRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        RenderTask renderTask = renderTaskRepository.findOne(id);
        renderTaskRepository.delete(renderTask);

    }

    @Override
    public void delete(RenderTask renderTask) {
        renderTaskRepository.delete(renderTask);
    }

    @Override
    public void deleteAll() {
        renderTaskRepository.deleteAll();
    }


    @Override
    public RenderTask getByQueueUUID(String queue_uuid) {
        for (RenderTask renderTask : listAll()) {
            if (queue_uuid.equals(renderTask.getServerQueueUUID())) {
                return renderTask;
            } else {
                LOG.error("No task found for this uuid");
            }

        }
        return null;

    }

    @Autowired
    public void setRenderTaskRepository(RenderTaskRepository renderTaskRepository) {
        this.renderTaskRepository = renderTaskRepository;
    }


}
