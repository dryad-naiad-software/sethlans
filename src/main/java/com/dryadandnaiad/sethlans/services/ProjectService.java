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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

/**
 * File created by Mario Estrella on 12/27/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface ProjectService {

    boolean startProject(String projectID);

    boolean resumeProject(String projectID);

    boolean pauseProject(String projectID);

    boolean stopProject(String projectID);

    void deleteProject(String projectID);

    void deleteAllProjectsByUser(String userID);

    void deleteAllProjects();

    boolean projectExists(String projectID);

    ResponseEntity<ProjectForm> projectFileUpload(MultipartFile projectFile);

    boolean createProject(ProjectForm projectForm);

    ProjectForm editProjectForm(String projectID);

    @Async
    void processCompletedRenders();

    boolean editProject(ProjectForm projectForm);
}
