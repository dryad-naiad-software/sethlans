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

import com.dryadandnaiad.sethlans.domains.database.queue.FrameFileUpdateItem;
import com.dryadandnaiad.sethlans.repositories.FrameFileUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 6/20/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class FrameFileUpdateDatabaseServiceImpl implements FrameFileUpdateDatabaseService {
    private FrameFileUpdateRepository frameFileUpdateRepository;
    @Override
    public List<FrameFileUpdateItem> listAll() {
        return new ArrayList<>(frameFileUpdateRepository.findAll());
    }

    @Override
    public FrameFileUpdateItem getById(Long id) {
        return frameFileUpdateRepository.findOne(id);
    }

    @Override
    public FrameFileUpdateItem saveOrUpdate(FrameFileUpdateItem domainObject) {
        return frameFileUpdateRepository.save(domainObject);
    }

    @Override
    public List<FrameFileUpdateItem> listByProjectUUID(String projectUUID) {
        List<FrameFileUpdateItem> projectList = new ArrayList<>();
        for (FrameFileUpdateItem frameFileUpdateItem : listAll()) {
            if (frameFileUpdateItem.getProjectUUID().equals(projectUUID)) {
                projectList.add(frameFileUpdateItem);
            }
        }
        return projectList;
    }

    @Override
    public void delete(Long id) {
        frameFileUpdateRepository.delete(id);
    }

    @Override
    public void delete(FrameFileUpdateItem frameFileUpdateItem) {
        frameFileUpdateRepository.delete(frameFileUpdateItem);
    }

    @Autowired
    public void setFrameFileUpdateRepository(FrameFileUpdateRepository frameFileUpdateRepository) {
        this.frameFileUpdateRepository = frameFileUpdateRepository;
    }
}
