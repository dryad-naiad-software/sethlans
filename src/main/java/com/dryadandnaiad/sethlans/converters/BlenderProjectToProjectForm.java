/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.ProjectFormProgress;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Created Mario Estrella on 4/2/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class BlenderProjectToProjectForm implements Converter<BlenderProject, ProjectForm> {
    @Override
    public ProjectForm convert(BlenderProject project) {
        ProjectForm projectForm = new ProjectForm();
        projectForm.setId(project.getId());
        projectForm.setVersion(project.getVersion());
        projectForm.setProjectName(project.getProjectName());
        projectForm.setBlenderEngine(project.getBlenderEngine());
        projectForm.setOutputFormat(project.getRenderOutputFormat());
        projectForm.setSelectedBlenderVersion(project.getBlenderVersion());
        projectForm.setRenderOn(project.getRenderOn());
        projectForm.setStartFrame(project.getStartFrame());
        projectForm.setEndFrame(project.getEndFrame());
        projectForm.setStepFrame(project.getStepFrame());
        projectForm.setProjectType(project.getProjectType());
        projectForm.setResolutionX(project.getResolutionX());
        projectForm.setResolutionY(project.getResolutionY());
        projectForm.setResPercentage(project.getResPercentage());
        projectForm.setSamples(project.getSamples());
        projectForm.setFileLocation(project.getBlendFileLocation());
        projectForm.setUploadedFile(project.getBlendFilename());
        projectForm.setStarted(project.isStarted());
        projectForm.setFinished(project.isFinished());
        projectForm.setProgress(ProjectFormProgress.DETAILS);
        projectForm.setPartsPerFrame(project.getPartsPerFrame());
        projectForm.setUuid(project.getProject_uuid());
        return projectForm;
    }
}
