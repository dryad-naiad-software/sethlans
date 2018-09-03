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

import com.dryadandnaiad.sethlans.domains.database.render.RenderTaskHistory;
import com.dryadandnaiad.sethlans.repositories.RenderTaskHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 9/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class RenderTaskHistoryDatabaseServiceImpl implements RenderTaskHistoryDatabaseService {
    private RenderTaskHistoryRepository renderTaskHistoryRepository;

    @Override
    public long tableSize() {
        return renderTaskHistoryRepository.count();
    }

    @Override
    public List<RenderTaskHistory> listAll() {
        return new ArrayList<>(renderTaskHistoryRepository.findAll());
    }

    @Override
    public RenderTaskHistory getById(Long id) {
        return renderTaskHistoryRepository.findOne(id);
    }

    @Override
    public RenderTaskHistory saveOrUpdate(RenderTaskHistory domainObject) {
        return renderTaskHistoryRepository.save(domainObject);
    }

    @Override
    public RenderTaskHistory findByQueueUUID(String queueUUID) {
        return renderTaskHistoryRepository.findByQueueUUID(queueUUID);
    }

    @Override
    public void delete(Long id) {
        renderTaskHistoryRepository.delete(id);
    }

    @Autowired
    public void setRenderTaskHistoryRepository(RenderTaskHistoryRepository renderTaskHistoryRepository) {
        this.renderTaskHistoryRepository = renderTaskHistoryRepository;
    }
}
