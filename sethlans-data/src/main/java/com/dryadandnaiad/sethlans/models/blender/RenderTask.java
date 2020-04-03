/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.models.blender;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.BaseEntity;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by Mario Estrella on 4/2/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class RenderTask extends BaseEntity {
    private String projectName;
    private String renderTaskUUID;
    private String connectionUUID;
    private String projectUUID;
    private String serverQueueUUID;
    private int samples;
    private String blendFilename;
    private String blendFileMD5Sum;
    private String blenderVersion;
    private String blenderExecutable;
    private int taskResolutionX;
    private int taskResolutionY;
    private int taskResPercentage;
    private boolean complete;
    private boolean inProgress;
    private String renderDir;
    private Long renderTime;
    private String deviceID;
    private boolean cancelRequestReceived;
    @Enumerated(value = EnumType.STRING)
    private BlenderEngine blenderEngine;
    @Enumerated(value = EnumType.STRING)
    private ComputeOn computeOn;
    @Enumerated(value = EnumType.STRING)
    private ImageOutputFormat imageOutputFormat;
    private Frame frame;
}
