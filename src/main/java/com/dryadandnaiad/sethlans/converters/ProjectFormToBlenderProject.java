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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.commands.ProjectForm;
import com.dryadandnaiad.sethlans.domains.blender.BlenderProject;
import com.dryadandnaiad.sethlans.utils.RandomString;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created Mario Estrella on 4/2/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class ProjectFormToBlenderProject implements Converter<ProjectForm, BlenderProject> {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectFormToBlenderProject.class);

    @Value("${sethlans.projectDir}")
    private String projectDir;

    @Override
    public BlenderProject convert(ProjectForm projectForm) {
        BlenderProject project = new BlenderProject();
        project.setId(projectForm.getId());
        project.setVersion(projectForm.getVersion());
        project.setProjectName(projectForm.getProjectName());
        project.setRenderOutputFormat(projectForm.getOutputFormat());
        project.setProjectType(projectForm.getProjectType());
        project.setStartFrame(projectForm.getStartFrame());
        project.setEndFrame(projectForm.getEndFrame());
        project.setStepFrame(projectForm.getStepFrame());
        project.setResolutionX(projectForm.getResolutionX());
        project.setResolutionY(projectForm.getResolutionY());
        project.setSamples(projectForm.getSamples());
        project.setResPercentage(projectForm.getResPercentage());
        project.setBlenderEngine(projectForm.getBlenderEngine());
        project.setBlendFilename(projectForm.getUploadedFile());
        project.setBlenderVersion(projectForm.getSelectedBlenderVersion());
        project.setRenderOn(projectForm.getRenderOn());


        if (projectForm.getId() == null) {
            project.setStarted(false);
            project.setFinished(false);
            project.setCurrentPercentage(0);

            RandomString randomString = new RandomString(6);

            File blenderProjectDirectory = new File(projectDir + File.separator + projectForm.getProjectName().replaceAll(" ", "_").toLowerCase() + "_" + randomString.nextString());


            try {
                if (!blenderProjectDirectory.mkdirs()) {
                    throw new Exception("Unable to create directory " + blenderProjectDirectory.toString());
                }
                File projecttemp = new File(projectForm.getFileLocation());
                if (projecttemp.renameTo(new File(blenderProjectDirectory + File.separator + projectForm.getUploadedFile()))) {
                    LOG.debug(projectForm.getUploadedFile() + " moved to " + blenderProjectDirectory.toString());
                    project.setBlendFileLocation(blenderProjectDirectory + File.separator + projectForm.getUploadedFile());
                } else {
                    throw new Exception(projectForm.getUploadedFile() + " failed to move");
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
                LOG.error(Throwables.getStackTraceAsString(e));
                return null;
            }
        } else {
            project.setBlendFileLocation(projectForm.getFileLocation());
        }


        return project;
    }

}
