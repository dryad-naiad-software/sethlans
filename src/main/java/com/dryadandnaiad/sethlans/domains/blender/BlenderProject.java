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

package com.dryadandnaiad.sethlans.domains.blender;

import com.dryadandnaiad.sethlans.domains.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class BlenderProject extends AbstractEntityClass {
    private String projectName;
    private RenderOutputFormat renderOutputFormat;
    private ProjectType projectType;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int samples;
    private BlenderEngine blenderEngine;
    private int resolutionX;
    private int resolutionY;
    private int resPercentage;
    private String blendFilename;
    private String blendFileLocation;

    @ManyToOne
    private BlenderBinary blenderBinary;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public RenderOutputFormat getRenderOutputFormat() {
        return renderOutputFormat;
    }

    public void setRenderOutputFormat(RenderOutputFormat renderOutputFormat) {
        this.renderOutputFormat = renderOutputFormat;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
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

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public BlenderEngine getBlenderEngine() {
        return blenderEngine;
    }

    public void setBlenderEngine(BlenderEngine blenderEngine) {
        this.blenderEngine = blenderEngine;
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

    public String getBlendFilename() {
        return blendFilename;
    }

    public void setBlendFilename(String blendFilename) {
        this.blendFilename = blendFilename;
    }

    public String getBlendFileLocation() {
        return blendFileLocation;
    }

    public void setBlendFileLocation(String blendFileLocation) {
        this.blendFileLocation = blendFileLocation;
    }

    public BlenderBinary getBlenderBinary() {
        return blenderBinary;
    }

    public void setBlenderBinary(BlenderBinary blenderBinary) {
        this.blenderBinary = blenderBinary;
    }
}
