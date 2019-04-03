/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.queue.QueueHistoryItem;
import com.dryadandnaiad.sethlans.repositories.QueueHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 4/2/2019.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class QueueHistoryDatabaseServiceImpl implements QueueHistoryDatabaseService {
    private QueueHistoryRepository queueHistoryRepository;

    @Override
    public long tableSize() {
        return queueHistoryRepository.count();
    }

    @Override
    public List<QueueHistoryItem> listAll() {
        return new ArrayList<>(queueHistoryRepository.findAll());
    }

    @Override
    public QueueHistoryItem getById(Long id) {
        return queueHistoryRepository.findOne(id);
    }

    @Override
    public QueueHistoryItem saveOrUpdate(QueueHistoryItem domainObject) {
        return queueHistoryRepository.save(domainObject);
    }


    @Override
    public void delete(Long id) {
        QueueHistoryItem queueHistoryItem = queueHistoryRepository.findOne(id);
        queueHistoryRepository.delete(queueHistoryItem);

    }

    @Override
    public QueueHistoryItem findQueueHistoryItemToUpdate(String queueUUID, String nodeName, String deviceId) {
        return queueHistoryRepository.findByQueueItemUUIDAndNodeNameAndDeviceIdAndFailedIsFalseAndCompleteIsFalse(queueUUID, nodeName, deviceId);
    }

    @Override
    public QueueHistoryItem findQueueHistoryItemToPause(String queueUUID, String deviceId) {
        return queueHistoryRepository.findByQueueItemUUIDAndDeviceIdAndCompleteIsFalse(queueUUID, deviceId);
    }

    @Autowired
    public void setQueueHistoryRepository(QueueHistoryRepository queueHistoryRepository) {
        this.queueHistoryRepository = queueHistoryRepository;
    }


}
