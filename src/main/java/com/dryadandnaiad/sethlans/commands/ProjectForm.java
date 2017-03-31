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

import com.dryadandnaiad.sethlans.domains.blender.BlendFile;
import com.dryadandnaiad.sethlans.domains.blender.BlenderZipEntity;
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
    private String fileLocation;
    private String uploadedFile;
    private BlenderZipEntity selectedBlenderBinary;
    private List<BlenderZipEntity> availableBlenderBinaries;
    private BlendFile blendFile;
    private int samples;

    public ProjectFormProgress getProgress() {
        return progress;
    }

    public void setProgress(ProjectFormProgress progress) {
        this.progress = progress;
    }

    public List<BlenderZipEntity> getAvailableBlenderBinaries() {
        return availableBlenderBinaries;
    }

    public void setAvailableBlenderBinaries(List<BlenderZipEntity> availableBlenderBinaries) {
        this.availableBlenderBinaries = availableBlenderBinaries;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public BlenderZipEntity getSelectedBlenderBinary() {
        return selectedBlenderBinary;
    }

    public void setSelectedBlenderBinary(BlenderZipEntity selectedBlenderBinary) {
        this.selectedBlenderBinary = selectedBlenderBinary;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(String uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public BlendFile getBlendFile() {
        return blendFile;
    }

    public void setBlendFile(BlendFile blendFile) {
        this.blendFile = blendFile;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    @Override
    public String toString() {
        return "ProjectForm{" +
                "projectName='" + projectName + '\'' +
                ", progress=" + progress +
                ", fileLocation='" + fileLocation + '\'' +
                ", uploadedFile='" + uploadedFile + '\'' +
                ", selectedBlenderBinary=" + selectedBlenderBinary +
                ", availableBlenderBinaries=" + availableBlenderBinaries +
                ", blendFile=" + blendFile +
                ", samples=" + samples +
                '}';
    }
}
