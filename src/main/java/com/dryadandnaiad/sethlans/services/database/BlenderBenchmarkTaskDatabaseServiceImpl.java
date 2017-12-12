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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.repositories.BlenderBenchmarkTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created Mario Estrella on 12/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderBenchmarkTaskDatabaseServiceImpl implements BlenderBenchmarkTaskDatabaseService {
    private BlenderBenchmarkTaskRepository blenderBenchmarkTaskRepository;

    @Override
    public List<BlenderBenchmarkTask> listAll() {
        return null;
    }

    @Override
    public BlenderBenchmarkTask getById(Integer id) {
        return null;
    }

    @Override
    public BlenderBenchmarkTask saveOrUpdate(BlenderBenchmarkTask domainObject) {
        return null;
    }

    @Override
    public void delete(Integer id) {

    }

    @Autowired
    public void setBlenderBenchmarkTaskRepository(BlenderBenchmarkTaskRepository blenderBenchmarkTaskRepository) {
        this.blenderBenchmarkTaskRepository = blenderBenchmarkTaskRepository;
    }
}
