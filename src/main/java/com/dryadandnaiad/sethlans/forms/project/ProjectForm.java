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

package com.dryadandnaiad.sethlans.forms.project;

import com.dryadandnaiad.sethlans.domains.blender.BlendFile;
import com.dryadandnaiad.sethlans.domains.blender.VideoSettings;
import com.dryadandnaiad.sethlans.enums.*;
import lombok.Data;

import java.util.UUID;

/**
 * Created Mario Estrella on 3/7/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
public class ProjectForm {
    private String projectName;
    private int samples;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int resolutionX;
    private int resolutionY;
    private int resPercentage;
    private boolean useParts;
    private ProjectType projectType;
    private ProjectStatus projectStatus;
    private BlenderEngine blenderEngine;
    private String uploadedFile;
    private String fileLocation;
    private String selectedBlenderversion;
    private ComputeType renderOn;
    private VideoSettings videoSettings;
    private ImageOutputFormat imageOutputFormat;
    private AnimationType animationType;
    private String username;
    private String uuid;
    private int partsPerFrame;
    public ProjectForm() {
        this.startFrame = 1;
        this.endFrame = 200;
        this.stepFrame = 1;
        this.projectType = ProjectType.STILL_IMAGE;
        this.renderOn = ComputeType.CPU_GPU;
        this.imageOutputFormat = ImageOutputFormat.PNG;
        this.animationType = AnimationType.Images;
        this.uuid = UUID.randomUUID().toString();
        this.partsPerFrame = 4;
    }

    public void populateForm(BlendFile blendFile) {
        this.samples = blendFile.getCyclesSamples();
        this.startFrame = blendFile.getFrameStart();
        this.endFrame = blendFile.getFrameEnd();
        this.stepFrame = blendFile.getFrameStep();
        this.resolutionX = blendFile.getResolutionX();
        this.resolutionY = blendFile.getResolutionY();
        this.resPercentage = blendFile.getResPercent();
        this.blenderEngine = blendFile.getEngine();

    }
}
