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

import com.dryadandnaiad.sethlans.converters.ProjectFormToBlenderProject;
import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.forms.project.ProjectForm;
import com.dryadandnaiad.sethlans.repositories.BlenderProjectRepository;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    private Gson gson = new GsonBuilder().setLenient().create();
    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectDatabaseServiceImpl.class);

    @Override
    public long tableSize() {
        return blenderProjectRepository.count();
    }

    @Override
    public List<BlenderProject> listAll() {
        List<BlenderProject> all = new ArrayList<>(blenderProjectRepository.findAll());
        for (BlenderProject blenderProject : all) {
            blenderProject.setFramePartList(loadBlenderPartList(blenderProject));
        }
        return all;
    }

    @Override
    public List<BlenderProject> listWithoutFramePart() {
        return new ArrayList<>(blenderProjectRepository.findAll());
    }

    @Override
    public int listSize() {
        return blenderProjectRepository.findAll().size();
    }

    @Override
    public int listSizeByUser(String username) {
        List<BlenderProject> listToReturn = new ArrayList<>();
        for (BlenderProject project : blenderProjectRepository.findAll()) {
            if (project.getSethlansUser().getUsername().equals(username)) {
                listToReturn.add(project);
            }

        }
        return listToReturn.size();
    }

    @Override
    public List<BlenderProject> getProjectsByUser(String username) {
        List<BlenderProject> listToReturn = new ArrayList<>();
        for (BlenderProject project : listAll()) {
            if (project.getSethlansUser().getUsername().equals(username)) {
                listToReturn.add(project);
            }

        }
        return listToReturn;
    }

    @Override
    public List<BlenderProject> getProjectsByUserWithoutFrameParts(String username) {
        List<BlenderProject> listToReturn = new ArrayList<>();
        for (BlenderProject project : listWithoutFramePart()) {
            if (project.getSethlansUser().getUsername().equals(username)) {
                listToReturn.add(project);
            }

        }
        return listToReturn;
    }

    @Override
    public List<BlenderProject> getPendingProjects() {
        List<BlenderProject> pendingProjects = new ArrayList<>();
        for (BlenderProject blenderProject : listAll()) {
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Pending)) {
                pendingProjects.add(blenderProject);
            }

        }
        return pendingProjects;
    }

    @Override
    public int pendingProjectsSize() {
        List<BlenderProject> pendingProjects = new ArrayList<>();
        for (BlenderProject blenderProject : blenderProjectRepository.findAll()) {
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Pending)) {
                pendingProjects.add(blenderProject);
            }

        }
        return pendingProjects.size();

    }

    @Override
    public List<BlenderProject> getRemainingQueueProjects() {
        List<BlenderProject> remainingProjects = new ArrayList<>();
        for (BlenderProject blenderProject : listAll()) {
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Rendering) || blenderProject.getProjectStatus().equals(ProjectStatus.Started)) {
                if (!blenderProject.isQueueFillComplete()) {
                    remainingProjects.add(blenderProject);
                }
            }
        }
        return remainingProjects;
    }

    @Override
    public int remainingQueueProjectsSize() {
        List<BlenderProject> remainingProjects = new ArrayList<>();
        for (BlenderProject blenderProject : blenderProjectRepository.findAll()) {
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Rendering) || blenderProject.getProjectStatus().equals(ProjectStatus.Started)) {
                if (!blenderProject.isQueueFillComplete()) {
                    remainingProjects.add(blenderProject);
                }
            }
        }
        return remainingProjects.size();
    }

    @Override
    public boolean deleteWithVerification(String username, Long id) {
        BlenderProject blenderProject = blenderProjectRepository.findOne(id);
        if (blenderProject.getSethlansUser().getUsername().equals(username)) {
            delete(id);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public List<BlenderProject> listAllReverse() {
        return new ArrayList<>(Lists.reverse(listAll()));
    }

    @Override
    public BlenderProject getById(Long id) {
        BlenderProject blenderProject = blenderProjectRepository.findOne(id);
        blenderProject.setFramePartList(loadBlenderPartList(blenderProject));
        return blenderProject;
    }

    @Override
    public BlenderProject getByIdWithoutFrameParts(Long id) {
        return blenderProjectRepository.findOne(id);
    }

    @Override
    public BlenderProject getProjectByUser(String username, Long id) {
        BlenderProject blenderProject = blenderProjectRepository.findOne(id);
        if (blenderProject.getSethlansUser().getUsername().equals(username)) {
            blenderProject.setFramePartList(loadBlenderPartList(blenderProject));
            return blenderProject;
        } else {
            return null;
        }
    }

    @Override
    public BlenderProject getProjectByUserWithoutFrameParts(String username, Long id) {
        BlenderProject blenderProject = blenderProjectRepository.findOne(id);
        if (blenderProject.getSethlansUser().getUsername().equals(username)) {
            return blenderProject;
        } else {
            return null;
        }

    }


    @Override
    public BlenderProject getByProjectUUID(String projectUUID) {
        List<BlenderProject> blenderProjectList = listAll();
        for (BlenderProject blenderProject : blenderProjectList) {
            if (blenderProject.getProject_uuid().equals(projectUUID)) {
                blenderProject.setFramePartList(loadBlenderPartList(blenderProject));
                return blenderProject;
            }
        }
        return null;
    }

    @Override
    public BlenderProject getByProjectUUIDWithoutFrameParts(String projectUUID) {
        List<BlenderProject> blenderProjectList = listWithoutFramePart();
        for (BlenderProject blenderProject : blenderProjectList) {
            if (blenderProject.getProject_uuid().equals(projectUUID)) {
                return blenderProject;
            }
        }
        return null;
    }

    @Override
    public BlenderProject saveOrUpdate(BlenderProject domainObject) {
        saveBlenderPartList(domainObject);
        return blenderProjectRepository.save(domainObject);
    }

    private void saveBlenderPartList(BlenderProject blenderProject) {
        try {
            Writer writer = new FileWriter(blenderProject.getProjectRootDir() + File.separator + blenderProject.getProject_uuid() + ".json");
            gson.toJson(blenderProject.getFramePartList(), writer);
            writer.close();
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

    private List<BlenderFramePart> loadBlenderPartList(BlenderProject blenderProject) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(blenderProject.getProjectRootDir() +
                    File.separator + blenderProject.getProject_uuid() + ".json"));
            List<BlenderFramePart> blenderFramePartList = gson.fromJson(reader, new TypeToken<List<BlenderFramePart>>() {
            }.getType());
            reader.close();
            return blenderFramePartList;
        } catch (EOFException e) {
            LOG.debug("End of file reached");
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));

        }
        return null;
    }

    @Override
    public BlenderProject saveOrUpdateProjectForm(ProjectForm projectForm) {
        return saveOrUpdate(projectFormToBlenderProject.convert(projectForm));
    }

    @Override
    public void delete(Long id) {
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
