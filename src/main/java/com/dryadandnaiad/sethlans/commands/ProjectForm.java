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

package com.dryadandnaiad.sethlans.commands;

import com.dryadandnaiad.sethlans.domains.BlenderFile;
import com.dryadandnaiad.sethlans.enums.ProjectFormProgress;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created Mario Estrella on 3/25/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class ProjectForm {
    @NotNull
    @NotEmpty
    private String projectName;


    private ProjectFormProgress progress = ProjectFormProgress.UPLOAD;
    private BlenderFile blenderFile;
    private List<BlenderFile> serverBlenderBinaries;

    public BlenderFile getBlenderFile() {
        return blenderFile;
    }

    public void setBlenderFile(BlenderFile blenderFile) {
        this.blenderFile = blenderFile;
    }

    public ProjectFormProgress getProgress() {
        return progress;
    }

    public void setProgress(ProjectFormProgress progress) {
        this.progress = progress;
    }

    public List<BlenderFile> getServerBlenderBinaries() {
        return serverBlenderBinaries;
    }

    public void setServerBlenderBinaries(List<BlenderFile> serverBlenderBinaries) {
        this.serverBlenderBinaries = serverBlenderBinaries;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "ProjectForm{" +
                "projectName='" + projectName + '\'' +
                ", progress=" + progress +
                ", blenderFile=" + blenderFile +
                ", serverBlenderBinaries=" + serverBlenderBinaries +
                '}';
    }
}
