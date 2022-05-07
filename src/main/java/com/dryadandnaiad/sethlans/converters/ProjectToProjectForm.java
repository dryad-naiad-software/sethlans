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

import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * File created by Mario Estrella on 12/28/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

@Component
public class ProjectToProjectForm implements Converter<Project, ProjectForm> {
    private final BlenderArchiveRepository blenderArchiveRepository;

    public ProjectToProjectForm(BlenderArchiveRepository blenderArchiveRepository) {
        this.blenderArchiveRepository = blenderArchiveRepository;
    }


    @Override
    public ProjectForm convert(Project project) {
        var blenderArchiveList = blenderArchiveRepository.findAllByDownloadedIsTrue();
        var versions = new HashSet<String>();
        for (BlenderArchive blenderArchive : blenderArchiveList) {
            versions.add(blenderArchive.getBlenderVersion());
        }
        var versionList = new ArrayList<String>(versions);
        versionList.sort(new AlphaNumericComparator());
        Collections.reverse(versionList);
        var projectForm = ProjectForm.builder()
                .projectID(project.getProjectID())
                .projectSettings(project.getProjectSettings())
                .projectName(project.getProjectName())
                .projectType(project.getProjectType())
                .installedBlenderVersions(versionList)
                .build();


        return projectForm;
    }
}
