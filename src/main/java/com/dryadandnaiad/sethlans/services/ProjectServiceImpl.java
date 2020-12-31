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

import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * File created by Mario Estrella on 12/27/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean startProject(String projectID) {
        return false;
    }

    @Override
    public boolean resumeProject(String projectID) {
        return false;
    }

    @Override
    public boolean pauseProject(String projectID) {
        return false;
    }

    @Override
    public boolean stopProject(String projectID) {
        return false;
    }

    @Override
    public void deleteProject(String projectID) {
        if (projectRepository.getProjectByProjectID(projectID).isPresent()) {
            var project = projectRepository.getProjectByProjectID(projectID).get();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
                projectRepository.delete(project);
            } else {
                if (project.getUser().getUsername().equals(auth.getName())) {
                    projectRepository.delete(project);
                }
            }
        } else {
            log.info("Unable to delete, there is no project with project id: " + projectID);
        }
    }

    @Override
    public void deleteAllProjectsByUser(String userID) {
        if (userRepository.findUserByUserID(userID).isPresent()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
                projectRepository.deleteAllByUser(userRepository.findUserByUserID(userID).get());
            } else {
                if (userRepository.findUserByUserID(userID).get().getUsername().equals(auth.getName())) {
                    projectRepository.deleteAllByUser(userRepository.findUserByUserID(userID).get());
                }
            }
        }
    }

    @Override
    public void deleteAllProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            projectRepository.deleteAll();
        } else {
            if (userRepository.findUserByUsername(auth.getName()).isPresent())
                projectRepository.deleteAllByUser(userRepository.findUserByUsername(auth.getName()).get());
        }
    }

    @Override
    public boolean projectExists(String projectID) {
        return projectRepository.getProjectByProjectID(projectID).isPresent();
    }

}
