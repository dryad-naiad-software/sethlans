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
import lombok.Data;
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
    private int taskResolutionX;
    private int taskResolutionY;
    private int partResPercentage;
    private boolean complete;
    private boolean inProgress;
    private String renderDir;

}
