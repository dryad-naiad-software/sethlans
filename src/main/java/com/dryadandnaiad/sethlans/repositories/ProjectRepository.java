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

package com.dryadandnaiad.sethlans.repositories;

import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.user.SethlansUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> getProjectsBySethlansUser(SethlansUser sethlansUser);

    Optional<Project> getProjectByProjectID(String projectID);

    Optional<Project> getProjectBySethlansUserAndProjectID(SethlansUser sethlansUser, String projectID);

    void deleteAllBySethlansUser(SethlansUser sethlansUser);
}
