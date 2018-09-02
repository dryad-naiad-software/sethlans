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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.repositories.BlenderBinaryRepository;
import com.dryadandnaiad.sethlans.utils.AlphanumComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created Mario Estrella on 3/23/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderBinaryDatabaseServiceImpl implements BlenderBinaryDatabaseService {

    private BlenderBinaryRepository blenderBinaryRepository;

    @Override
    public long tableSize() {
        return blenderBinaryRepository.count();
    }

    @Override
    public List<BlenderBinary> listAll() {
        return new ArrayList<>(blenderBinaryRepository.findAll());
    }

    @Override
    public List<String> installedBlenderVersions() {
        Set<String> installedBlenderVersions = new HashSet<>();
        List<BlenderBinary> binaries = listAll();
        for (BlenderBinary binary : binaries) {
            installedBlenderVersions.add(binary.getBlenderVersion());
        }
        List<String> sortedList = new ArrayList<>(installedBlenderVersions);
        sortedList.sort(new AlphanumComparator());
        Collections.reverse(sortedList);
        return sortedList;
    }

    @Override
    public String getHighestVersion() {
        List<String> versions = new ArrayList<>(installedBlenderVersions());
        versions.sort(new AlphanumComparator());
        return versions.get(0);
    }


    @Override
    public BlenderBinary getById(Long id) {
        return blenderBinaryRepository.findOne(id);
    }

    @Override
    public BlenderBinary saveOrUpdate(BlenderBinary domainObject) {
        return blenderBinaryRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        BlenderBinary blenderBinary = blenderBinaryRepository.findOne(id);
        blenderBinaryRepository.delete(blenderBinary);

    }

    @Autowired
    public void setBlenderBinaryRepository(BlenderBinaryRepository blenderBinaryRepository) {
        this.blenderBinaryRepository = blenderBinaryRepository;
    }
}
