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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.repositories.BlenderRenderTaskRepository;
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
public class BlenderRenderTaskDatabaseServiceImpl implements BlenderRenderTaskDatabaseService {

    private BlenderRenderTaskRepository blenderRenderTaskRepository;


    @Override
    public List<BlenderRenderTask> listAll() {
        List<BlenderRenderTask> blenderRenderTasks = new ArrayList<>();
        blenderRenderTaskRepository.findAll().forEach(blenderRenderTasks::add);
        return blenderRenderTasks;
    }

    @Override
    public BlenderRenderTask getById(Integer id) {
        return blenderRenderTaskRepository.findOne(id);
    }

    @Override
    public BlenderRenderTask saveOrUpdate(BlenderRenderTask domainObject) {
        return blenderRenderTaskRepository.save(domainObject);
    }

    @Override
    public void delete(Integer id) {
        BlenderRenderTask blenderRenderTask = blenderRenderTaskRepository.findOne(id);
        blenderRenderTaskRepository.delete(blenderRenderTask);

    }

    @Autowired
    public void setBlenderRenderTaskRepository(BlenderRenderTaskRepository blenderRenderTaskRepository) {
        this.blenderRenderTaskRepository = blenderRenderTaskRepository;
    }
}
