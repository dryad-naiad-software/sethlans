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

package com.dryadandnaiad.sethlans.domains.database.blender;

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.blender.VideoSettings;
import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
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
public class BlenderProject extends AbstractEntityClass implements Comparable<BlenderProject> {
    private ImageOutputFormat imageOutputFormat;
    private AnimationType animationType;
    private ProjectType projectType;
    private BlenderEngine blenderEngine;
    private ComputeType renderOn;
    @ManyToOne
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
    private boolean allImagesProcessed;
    private boolean userStopped;
    private ProjectStatus projectStatus;
    private VideoSettings videoSettings;
    private String projectName;
    private String blendFilename;
    private String blendFilenameMD5Sum;
    private String blendFileLocation;
    private String blenderVersion;
    private String currentFrameThumbnail;
    private String projectUUID;
    private String projectRootDir;
    private String movieFileLocation;
    private int totalNumberOfFrames;
    private int completedFrames;
    private int totalQueueSize;
    private int remainingQueueSize;
    private int queueIndex;
    private boolean queueFillComplete;
    private boolean reEncode;
    private Long totalRenderTime;
    private Long totalProjectTime;
    private Long timerStart;
    private Long timerEnd;
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> frameFileNames;
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> thumbnailFileNames;
    @Transient
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
                ", projectType=" + projectType +
                ", startFrame=" + startFrame +
                ", endFrame=" + endFrame +
                ", stepFrame=" + stepFrame +
                ", samples=" + samples +
                ", userStopped=" + userStopped +
                ", blenderEngine=" + blenderEngine +
                ", resolutionX=" + resolutionX +
                ", resolutionY=" + resolutionY +
                ", resPercentage=" + resPercentage +
                ", renderOn=" + renderOn +
                ", blendFilename='" + blendFilename + '\'' +
                ", blendFileLocation='" + blendFileLocation + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", currentFrameThumbnail='" + currentFrameThumbnail + '\'' +
                ", projectStatus ='" + projectStatus + '\'' +
                ", currentPercentage=" + currentPercentage +
                ", partsPerFrame=" + partsPerFrame +
                ", frameList=" + framePartList +
                ", project_uuid='" + projectUUID + '\'' +
                ", frameFileNames='" + frameFileNames + '\'' +
                ", projectRootDir='" + projectRootDir + '\'' +
                ", allImagesProcessed='" + allImagesProcessed + '\'' +
                ", totalNumOfFrames=" + getTotalNumOfFrames() +
                '}';
    }

    @Override
    public int compareTo(BlenderProject o) {
        if (getId() < o.getId()) {
            return 1;
        } else {
            return -1;
        }
    }
}
