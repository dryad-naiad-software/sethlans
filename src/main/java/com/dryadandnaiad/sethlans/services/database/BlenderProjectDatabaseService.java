/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.forms.project.ProjectForm;

import java.util.List;

/**
 * Created Mario Estrella on 4/2/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface BlenderProjectDatabaseService extends CRUDService<BlenderProject> {

    List<BlenderProject> listWithoutFramePart();

    long listSizeByUser(String username);

    List<BlenderProject> getProjectsByUserWithoutFrameParts(String username);

    List<BlenderProject> getPendingProjects();

    int pendingProjectsSize();

    List<BlenderProject> getRemainingQueueProjects();

    int remainingQueueProjectsSize();

    boolean deleteWithVerification(String username, Long id);

    BlenderProject getByIdWithoutFrameParts(Long id);

    BlenderProject getProjectByUser(String username, Long id);

    BlenderProject getProjectByUserWithoutFrameParts(String username, Long id);

    BlenderProject getByProjectUUID(String projectUUID);

    BlenderProject getByProjectUUIDWithoutFrameParts(String projectUUID);

    BlenderProject saveOrUpdateProjectForm(ProjectForm projectForm);

    void delete(BlenderProject blenderProject);
}
