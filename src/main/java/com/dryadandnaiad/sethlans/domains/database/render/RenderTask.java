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

package com.dryadandnaiad.sethlans.domains.database.render;

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class RenderTask extends AbstractEntityClass {
    private String projectName;
    private String renderTaskUUID;
    private String connectionUUID;
    private String projectUUID;
    private String serverQueueUUID;
    private ImageOutputFormat imageOutputFormat;
    private int samples;
    private BlenderEngine blenderEngine;
    private ComputeType computeType;
    private String blendFilename;
    private String blendFileMD5Sum;
    private String blenderVersion;
    private String blenderExecutable;
    private BlenderFramePart blenderFramePart;
    private int taskResolutionX;
    private int taskResolutionY;
    private int partResPercentage;
    private boolean complete;
    private boolean inProgress;
    private String renderDir;
    private Long renderTime;
    private String deviceID;
    private boolean cancelRequestReceived;

}
