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
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.enums.*;
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

    private Integer id;
    private Integer version;

    @NotNull
    @NotEmpty
    private String projectName;

    private ProjectFormProgress progress = ProjectFormProgress.UPLOAD;
    private int samples;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int resolutionX;
    private int resolutionY;
    private int resPercentage;
    private ProjectType projectType = ProjectType.STILL_IMAGE;
    private BlenderEngine blenderEngine;
    private String uploadedFile;
    private String fileLocation;
    private String selectedBlenderVersion;
    private ComputeType renderOn;
    private List<BlenderBinary> availableBlenderBinaries;
    private BlendFile blendFile;
    private RenderOutputFormat outputFormat;
    private boolean started;
    private boolean finished;
    private int currentPercentage;


    public void populateForm() {
        this.samples = blendFile.getCyclesSamples();
        this.startFrame = blendFile.getFrameStart();
        this.endFrame = blendFile.getFrameEnd();
        this.stepFrame = blendFile.getFrameStep();
        this.resolutionX = blendFile.getResolutionX();
        this.resolutionY = blendFile.getResolutionY();
        this.resPercentage = blendFile.getResPercent();
        this.blenderEngine = blendFile.getEngine();
        this.renderOn = ComputeType.CPU;
        this.started = false;
        this.finished = false;
        this.currentPercentage = 0;
        this.outputFormat = RenderOutputFormat.PNG;
    }


    public BlenderEngine getBlenderEngine() {
        return blenderEngine;
    }

    public void setBlenderEngine(BlenderEngine blenderEngine) {
        this.blenderEngine = blenderEngine;
    }

    public ComputeType getRenderOn() {
        return renderOn;
    }

    public void setRenderOn(ComputeType renderOn) {
        this.renderOn = renderOn;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ProjectFormProgress getProgress() {
        return progress;
    }

    public void setProgress(ProjectFormProgress progress) {
        this.progress = progress;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    public int getEndFrame() {
        return endFrame;
    }

    public void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }

    public int getStepFrame() {
        return stepFrame;
    }

    public void setStepFrame(int stepFrame) {
        this.stepFrame = stepFrame;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public void setResolutionX(int resolutionX) {
        this.resolutionX = resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public void setResolutionY(int resolutionY) {
        this.resolutionY = resolutionY;
    }

    public int getResPercentage() {
        return resPercentage;
    }

    public void setResPercentage(int resPercentage) {
        this.resPercentage = resPercentage;
    }

    public String getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(String uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public List<BlenderBinary> getAvailableBlenderBinaries() {
        return availableBlenderBinaries;
    }

    public void setAvailableBlenderBinaries(List<BlenderBinary> availableBlenderBinaries) {
        this.availableBlenderBinaries = availableBlenderBinaries;
    }

    public BlendFile getBlendFile() {
        return blendFile;
    }

    public void setBlendFile(BlendFile blendFile) {
        this.blendFile = blendFile;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public String getSelectedBlenderVersion() {
        return selectedBlenderVersion;
    }

    public void setSelectedBlenderVersion(String selectedBlenderVersion) {
        this.selectedBlenderVersion = selectedBlenderVersion;
    }

    public RenderOutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(RenderOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getCurrentPercentage() {
        return currentPercentage;
    }

    public void setCurrentPercentage(int currentPercentage) {
        this.currentPercentage = currentPercentage;
    }

    @Override
    public String toString() {
        return "ProjectForm{" +
                "id=" + id +
                ", version=" + version +
                ", projectName='" + projectName + '\'' +
                ", progress=" + progress +
                ", samples=" + samples +
                ", startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", stepFrame=" + stepFrame +
                ", resolutionX=" + resolutionX +
                ", resolutionY=" + resolutionY +
                ", resPercentage=" + resPercentage +
                ", projectType=" + projectType +
                ", blenderEngine=" + blenderEngine +
                ", uploadedFile='" + uploadedFile + '\'' +
                ", fileLocation='" + fileLocation + '\'' +
                ", selectedBlenderVersion='" + selectedBlenderVersion + '\'' +
                ", renderOn=" + renderOn +
                ", availableBlenderBinaries=" + availableBlenderBinaries +
                ", blendFile=" + blendFile +
                ", outputFormat=" + outputFormat +
                ", started=" + started +
                ", finished=" + finished +
                ", currentPercentage=" + currentPercentage +
                '}';
    }


}
