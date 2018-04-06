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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.forms.ProjectForm;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.utils.RandomString;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ProjectFormToBlenderProject implements Converter<ProjectForm, BlenderProject> {
    private SethlansUserDatabaseService sethlansUserDatabaseService;

    @Value("${sethlans.projectDir}")
    private String projectDir;

    private static final Logger LOG = LoggerFactory.getLogger(ProjectFormToBlenderProject.class);

    @Override
    public BlenderProject convert(ProjectForm projectForm) {
        BlenderProject blenderProject = new BlenderProject();
        blenderProject.setBlenderEngine(projectForm.getBlenderEngine());
        blenderProject.setProjectStatus(projectForm.getProjectStatus());
        blenderProject.setRenderOutputFormat(projectForm.getOutputFormat());
        blenderProject.setSamples(projectForm.getSamples());
        blenderProject.setRenderOn(projectForm.getRenderOn());
        blenderProject.setProject_uuid(projectForm.getUuid());
        blenderProject.setProjectName(projectForm.getProjectName());
        blenderProject.setBlenderVersion(projectForm.getSelectedBlenderversion());
        blenderProject.setBlendFileLocation(projectForm.getFileLocation());
        blenderProject.setSethlansUser(sethlansUserDatabaseService.findByUserName(projectForm.getUsername()));
        blenderProject.setProjectType(projectForm.getProjectType());
        blenderProject.setStartFrame(projectForm.getStartFrame());
        blenderProject.setEndFrame(projectForm.getEndFrame());
        blenderProject.setStepFrame(projectForm.getStepFrame());
        blenderProject.setResolutionX(projectForm.getResolutionX());
        blenderProject.setResolutionY(projectForm.getResolutionY());
        blenderProject.setResPercentage(projectForm.getResPercentage());
        blenderProject.setBlendFilename(projectForm.getUploadedFile());
        blenderProject.setPartsPerFrame(projectForm.getPartsPerFrame());
        blenderProject.setFrameRate(projectForm.getFrameRate());


        RandomString randomString = new RandomString(6);
        File blenderProjectDirectory = new File(projectDir + File.separator + projectForm.getProjectName().replaceAll(" ", "_").toLowerCase() + "_" + randomString.nextString());
        try {
            if (!blenderProjectDirectory.mkdirs()) {
                throw new Exception("Unable to create directory " + blenderProjectDirectory.toString());
            }
            File projecttemp = new File(projectForm.getFileLocation());
            if (projecttemp.renameTo(new File(blenderProjectDirectory + File.separator + projectForm.getUploadedFile()))) {
                LOG.debug(projectForm.getUploadedFile() + " moved to " + blenderProjectDirectory.toString());
                blenderProject.setBlendFileLocation(blenderProjectDirectory + File.separator + projectForm.getUploadedFile());
                blenderProject.setProjectRootDir(blenderProjectDirectory.toString());
            } else {
                throw new Exception(projectForm.getUploadedFile() + " failed to move");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
            return null;
        }

        return blenderProject;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
