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

import com.dryadandnaiad.sethlans.enums.ProjectState;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectStatus;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * File created by Mario Estrella on 12/28/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

@Component
public class ProjectFormToProject implements Converter<ProjectForm, Project> {
    private final UserRepository userRepository;

    public ProjectFormToProject(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public Project convert(ProjectForm projectForm) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findUserByUsername(auth.getName()).orElse(null);

        var project = Project.builder()
                .projectID(projectForm.getProjectID())
                .projectName(projectForm.getProjectName())
                .projectType(projectForm.getProjectType())
                .projectSettings(projectForm.getProjectSettings())
                .projectStatus(ProjectStatus.builder()
                        .projectState(ProjectState.ADDED)
                        .currentPercentage(0)
                        .completedFrames(0)
                        .totalQueueSize(0)
                        .queueIndex(0)
                        .remainingQueueSize(0)
                        .totalProjectTime(0L)
                        .totalRenderTime(0L)
                        .timerStart(0L)
                        .timerEnd(0L)
                        .allImagesProcessed(false)
                        .reEncode(false)
                        .userStopped(false)
                        .build())
                .user(user)
                .build();

        return project;
    }
}
