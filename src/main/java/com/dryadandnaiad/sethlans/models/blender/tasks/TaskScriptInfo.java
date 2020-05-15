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

package com.dryadandnaiad.sethlans.models.blender.tasks;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.DeviceType;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * File created by Mario Estrella on 5/5/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Getter
@Setter
@Builder
public class TaskScriptInfo {
    private Integer taskResolutionX;
    private Integer taskResolutionY;
    private Integer taskResPercentage;
    private Integer taskTileSize;
    private Integer samples;
    private Integer cores;
    private ComputeOn computeOn;
    private DeviceType deviceType;
    private BlenderEngine blenderEngine;
    private ImageOutputFormat imageOutputFormat;
    private List<String> deviceIDs;
}
