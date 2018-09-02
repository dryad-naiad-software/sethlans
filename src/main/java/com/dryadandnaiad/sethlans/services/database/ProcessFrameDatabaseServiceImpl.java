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

import com.dryadandnaiad.sethlans.domains.database.queue.ProcessFrameItem;
import com.dryadandnaiad.sethlans.repositories.ProcessFrameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 5/10/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class ProcessFrameDatabaseServiceImpl implements ProcessFrameDatabaseService {
    private ProcessFrameRepository processFrameRepository;

    @Override
    public long tableSize() {
        return processFrameRepository.count();
    }

    @Override
    public List<ProcessFrameItem> listAll() {
        return new ArrayList<>(processFrameRepository.findAll());
    }

    @Override
    public ProcessFrameItem getById(Long id) {
        return processFrameRepository.findOne(id);
    }

    @Override
    public List<ProcessFrameItem> listbyProjectUUID(String projectUUID) {
        List<ProcessFrameItem> projectList = new ArrayList<>();
        for (ProcessFrameItem processFrameItem : listAll()) {
            if (processFrameItem.getProjectUUID().equals(projectUUID)) {
                projectList.add(processFrameItem);
            }
        }
        return projectList;
    }

    @Override
    public ProcessFrameItem saveOrUpdate(ProcessFrameItem domainObject) {
        return processFrameRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        processFrameRepository.delete(id);
    }

    @Override
    public void delete(ProcessFrameItem processFrameItem) {
        processFrameRepository.delete(processFrameItem);
    }

    @Autowired
    public void setProcessFrameRepository(ProcessFrameRepository processFrameRepository) {
        this.processFrameRepository = processFrameRepository;
    }
}
