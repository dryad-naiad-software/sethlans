/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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
    private int samples;
    private BlenderEngine blenderEngine;
    private ComputeType computeType;
    private String blendFilename;
    private String blenderVersion;
    private String blenderExecutable;
    private BlenderFramePart blenderFramePart;
    private boolean complete;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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

    public RenderOutputFormat getRenderOutputFormat() {
        return renderOutputFormat;
    }

    public void setRenderOutputFormat(RenderOutputFormat renderOutputFormat) {
        this.renderOutputFormat = renderOutputFormat;
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

    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
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

    public String getBlenderExecutable() {
        return blenderExecutable;
    }

    public void setBlenderExecutable(String blenderExecutable) {
        this.blenderExecutable = blenderExecutable;
    }

    public BlenderFramePart getBlenderFramePart() {
        return blenderFramePart;
    }

    public void setBlenderFramePart(BlenderFramePart blenderFramePart) {
        this.blenderFramePart = blenderFramePart;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public String toString() {
        return "BlenderRenderTask{" +
                "projectName='" + projectName + '\'' +
                ", connection_uuid='" + connection_uuid + '\'' +
                ", project_uuid='" + project_uuid + '\'' +
                ", renderOutputFormat=" + renderOutputFormat +
                ", samples=" + samples +
                ", blenderEngine=" + blenderEngine +
                ", computeType=" + computeType +
                ", blendFilename='" + blendFilename + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", blenderExecutable='" + blenderExecutable + '\'' +
                ", blenderFramePart=" + blenderFramePart +
                ", complete=" + complete +
                '}';
    }
}
