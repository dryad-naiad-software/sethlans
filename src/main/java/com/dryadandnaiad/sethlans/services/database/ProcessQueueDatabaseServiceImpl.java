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

import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.repositories.ProcessQueueRepository;
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
    public long tableSize() {
        return processQueueRepository.count();
    }

    @Override
    public List<ProcessQueueItem> listAll() {
        return new ArrayList<>(processQueueRepository.findAll());
    }

    @Override
    public ProcessQueueItem getProcessByQueueUUID(String queueUUID) {
        return processQueueRepository.findProcessQueueItemByQueueUUID(queueUUID);
    }

    @Override
    public List<ProcessQueueItem> getProcessListByProjectUUID(String projectUUID) {
        return processQueueRepository.findProcessQueueItemsByProjectUUID(projectUUID);
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
        processQueueRepository.delete(processQueueRepository.findOne(id));
    }

    @Override
    public void delete(ProcessQueueItem processQueueItem) {
        processQueueRepository.delete(processQueueItem);

    }

    @Autowired
    public void setProcessQueueRepository(ProcessQueueRepository processQueueRepository) {
        this.processQueueRepository = processQueueRepository;
    }
}
