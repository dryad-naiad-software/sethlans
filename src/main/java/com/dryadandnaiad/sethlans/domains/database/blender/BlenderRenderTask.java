/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.domains.database.blender;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class BlenderRenderTask extends AbstractEntityClass {
    private String projectName;
    private String connection_uuid;
    private String project_uuid;
    private RenderOutputFormat renderOutputFormat;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int samples;
    private BlenderEngine blenderEngine;
    private int resolutionX;
    private int resolutionY;
    private int resPercentage;
    private ComputeType computeType;
    private int currentPercentage;
    private String blendFilename;
    private String blenderVersion;
    private int part;
    private String blenderExecutable;

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

    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public int getCurrentPercentage() {
        return currentPercentage;
    }

    public void setCurrentPercentage(int currentPercentage) {
        this.currentPercentage = currentPercentage;
    }

    public String getBlendFilename() {
        return blendFilename;
    }

    public void setBlendFilename(String blendFilename) {
        this.blendFilename = blendFilename;
    }

    public String getBlenderVersion() {
        return blenderVersion;
    }

    public void setBlenderVersion(String blenderVersion) {
        this.blenderVersion = blenderVersion;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public String getConnection_uuid() {
        return connection_uuid;
    }

    public void setConnection_uuid(String connection_uuid) {
        this.connection_uuid = connection_uuid;
    }

    public String getProject_uuid() {
        return project_uuid;
    }

    public void setProject_uuid(String project_uuid) {
        this.project_uuid = project_uuid;
    }

    public String getBlenderExecutable() {
        return blenderExecutable;
    }

    public void setBlenderExecutable(String blenderExecutable) {
        this.blenderExecutable = blenderExecutable;
    }

    @Override
    public String toString() {
        return "BlenderRenderTask{" +
                "projectName='" + projectName + '\'' +
                ", connection_uuid='" + connection_uuid + '\'' +
                ", project_uuid='" + project_uuid + '\'' +
                ", renderOutputFormat=" + renderOutputFormat +
                ", startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", stepFrame=" + stepFrame +
                ", samples=" + samples +
                ", blenderEngine=" + blenderEngine +
                ", resolutionX=" + resolutionX +
                ", resolutionY=" + resolutionY +
                ", resPercentage=" + resPercentage +
                ", computeType=" + computeType +
                ", currentPercentage=" + currentPercentage +
                ", blendFilename='" + blendFilename + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", part=" + part +
                ", blenderExecutable='" + blenderExecutable + '\'' +
                '}';
    }
}
