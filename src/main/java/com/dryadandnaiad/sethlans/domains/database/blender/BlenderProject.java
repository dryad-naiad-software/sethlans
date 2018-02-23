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
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.List;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class BlenderProject extends AbstractEntityClass {
    private RenderOutputFormat renderOutputFormat;
    private ProjectType projectType;
    private BlenderEngine blenderEngine;
    private ComputeType renderOn;
    @ManyToOne(fetch = FetchType.LAZY)
    private SethlansUser sethlansUser;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int samples;
    private int resolutionX;
    private int resolutionY;
    private int resPercentage;
    private int currentPercentage;
    private int partsPerFrame;
    private boolean started;
    private boolean finished;
    private boolean allImagesProcessed;
    private boolean paused;
    private String projectName;
    private String blendFilename;
    private String blendFileLocation;
    private String blenderVersion;
    private String currentFrameThumbnail;
    private String project_uuid;
    private String projectRootDir;
    @ElementCollection
    private List<String> frameFileNames;
    @ElementCollection
    private List<BlenderFramePart> framePartList;


    public int getTotalNumOfFrames() {
        int frameSum = (endFrame - startFrame) / stepFrame;
        frameSum = frameSum + 1;
        if ((frameSum > 1)) {
            return frameSum;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "BlenderProject{" +
                "projectName='" + projectName + '\'' +
                ", sethlansUser=" + sethlansUser +
                ", renderOutputFormat=" + renderOutputFormat +
                ", projectType=" + projectType +
                ", startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", stepFrame=" + stepFrame +
                ", samples=" + samples +
                ", blenderEngine=" + blenderEngine +
                ", resolutionX=" + resolutionX +
                ", resolutionY=" + resolutionY +
                ", resPercentage=" + resPercentage +
                ", renderOn=" + renderOn +
                ", blendFilename='" + blendFilename + '\'' +
                ", blendFileLocation='" + blendFileLocation + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", currentFrameThumbnail='" + currentFrameThumbnail + '\'' +
                ", started=" + started +
                ", finished=" + finished +
                ", paused=" + paused +
                ", currentPercentage=" + currentPercentage +
                ", partsPerFrame=" + partsPerFrame +
                ", frameList=" + framePartList +
                ", project_uuid='" + project_uuid + '\'' +
                ", frameFileNames='" + frameFileNames + '\'' +
                ", projectRootDir='" + projectRootDir + '\'' +
                ", allImagesProcessed='" + allImagesProcessed + '\'' +
                ", totalNumOfFrames=" + getTotalNumOfFrames() +
                '}';
    }
}
