/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.BlenderFileEntity;
import com.dryadandnaiad.sethlans.repositories.BlenderEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 3/23/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderFileServiceImpl implements BlenderFileService {

    private BlenderEntityRepository blenderEntityRepository;

    @Autowired
    public void setBlenderEntityRepository(BlenderEntityRepository blenderEntityRepository) {
        this.blenderEntityRepository = blenderEntityRepository;
    }

    @Override
    public List<?> listAll() {
        List<BlenderFileEntity> blenderEntities = new ArrayList<>();
        blenderEntityRepository.findAll().forEach(blenderEntities::add);
        return blenderEntities;
    }

    @Override
    public BlenderFileEntity getById(Integer id) {
        return blenderEntityRepository.findOne(id);
    }

    @Override
    public BlenderFileEntity saveOrUpdate(BlenderFileEntity domainObject) {
        return blenderEntityRepository.save(domainObject);
    }

    @Override
    public void delete(Integer id) {
        BlenderFileEntity blenderFileEntity = blenderEntityRepository.findOne(id);
        blenderEntityRepository.delete(blenderFileEntity);

    }
}
