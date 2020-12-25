/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectView;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class ProjectToProjectView implements Converter<Project, ProjectView> {
    @Override
    public ProjectView convert(Project project) {
        var projectView = ProjectView.builder().id(project.getId())
                .projectName(project.getProjectName())
                .projectSettings(project.getProjectSettings())
                .projectStatus(project.getProjectStatus())
                .projectType(project.getProjectType())
                .thumbnailPresent(project.getThumbnailFileNames().size() > 0)
                .thumbnailURL(project.getThumbnailFileNames().get(project.getThumbnailFileNames().size() - 1))
                .userId(project.getUser().getId())
                .build();
        return projectView;
    }
}
