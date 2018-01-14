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

import com.dryadandnaiad.sethlans.commands.ProjectForm;
import com.dryadandnaiad.sethlans.converters.ProjectFormToBlenderProject;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.repositories.BlenderProjectRepository;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created Mario Estrella on 4/2/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderProjectDatabaseServiceImpl implements BlenderProjectDatabaseService {
    private BlenderProjectRepository blenderProjectRepository;
    private ProjectFormToBlenderProject projectFormToBlenderProject;
    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectDatabaseServiceImpl.class);
    private Set<BlenderProject> projectAccessed = new HashSet<>();

    @Override
    public List<BlenderProject> listAll() {
        List<BlenderProject> blenderProjects = new ArrayList<>();
        blenderProjectRepository.findAll().forEach(blenderProjects::add);
        return blenderProjects;
    }

    @Override
    public List<BlenderProject> listAllReverse() {
        return new ArrayList<>(Lists.reverse(listAll()));
    }

    @Override
    public BlenderProject getById(Integer id) {
        return blenderProjectRepository.findOne(id);
    }

    @Override
    public Boolean isProjectDBEntryInUse(String projectUUID) {
        return projectAccessed.contains(getByProjectUUID(projectUUID));
    }

    @Override
    public Boolean isProjectDBEntryInUse(BlenderProject blenderProject) {
        return projectAccessed.contains(blenderProject);
    }

    @Override
    public BlenderProject getByProjectUUID(String projectUUID) {
        List<BlenderProject> blenderProjectList = listAll();
        for (BlenderProject blenderProject : blenderProjectList) {
            if (blenderProject.getProject_uuid().equals(projectUUID)) {
                projectAccessed.add(blenderProject);
                return blenderProject;
            }
        }
        return null;
    }

    @Override
    public BlenderProject saveOrUpdate(BlenderProject domainObject) {
        projectAccessed.remove(domainObject);
        return blenderProjectRepository.save(domainObject);
    }

    @Override
    public BlenderProject saveOrUpdateProjectForm(ProjectForm projectForm) {
        return saveOrUpdate(projectFormToBlenderProject.convert(projectForm));
    }

    @Override
    public void delete(Integer id) {
        BlenderProject blenderProject = blenderProjectRepository.findOne(id);
        blenderProjectRepository.delete(blenderProject);
    }

    @Override
    public void delete(BlenderProject blenderProject) {
        blenderProjectRepository.delete(blenderProject);
    }

    @Autowired
    public void setBlenderProjectRepository(BlenderProjectRepository blenderProjectRepository) {
        this.blenderProjectRepository = blenderProjectRepository;
    }

    @Autowired
    public void setProjectFormToBlenderProject(ProjectFormToBlenderProject projectFormToBlenderProject) {
        this.projectFormToBlenderProject = projectFormToBlenderProject;
    }

}
