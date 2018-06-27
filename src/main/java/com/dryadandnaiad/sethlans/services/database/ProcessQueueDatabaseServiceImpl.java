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

import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.repositories.ProcessQueueRepository;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 3/30/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class ProcessQueueDatabaseServiceImpl implements ProcessQueueDatabaseService {
    private ProcessQueueRepository processQueueRepository;
    private static final Logger LOG = LoggerFactory.getLogger(ProcessQueueDatabaseServiceImpl.class);

    @Override
    public List<ProcessQueueItem> listAll() {
        return new ArrayList<>(processQueueRepository.findAll());
    }

    @Override
    public ProcessQueueItem getProcessByQueueItem(String queueUUID) {
        for (ProcessQueueItem processQueueItem : listAll()) {
            if (processQueueItem.getQueueUUID().equals(queueUUID)) {
                return processQueueItem;
            }
        }
        return null;
    }

    @Override
    public List<ProcessQueueItem> getListOfProcessByProject(String projectUUID) {
        List<ProcessQueueItem> processByProject = new ArrayList<>();
        for (ProcessQueueItem processQueueItem : listAll()) {
            if (processQueueItem.getProjectUUID().equals(projectUUID)) {
                processByProject.add(processQueueItem);
            }
        }
        return processByProject;
    }

    @Override
    public ProcessQueueItem getById(Long id) {
        return processQueueRepository.findOne(id);
    }

    @Override
    public ProcessQueueItem saveOrUpdate(ProcessQueueItem domainObject) {
        return processQueueRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        try {
            processQueueRepository.delete(processQueueRepository.findOne(id));
        } catch (Exception e) {
            LOG.error("Exception while deleting process queue item " + e.getMessage());
            LOG.debug(Throwables.getStackTraceAsString(e));
        }

    }

    @Override
    public void delete(ProcessQueueItem processQueueItem) {
        try {
            processQueueRepository.delete(processQueueItem);
        } catch (Exception e) {
            LOG.error("Exception while deleting process queue item " + e.getMessage());
            LOG.debug(Throwables.getStackTraceAsString(e));
        }
    }

    @Autowired
    public void setProcessQueueRepository(ProcessQueueRepository processQueueRepository) {
        this.processQueueRepository = processQueueRepository;
    }
}
