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
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * File created by Mario Estrella on 4/2/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Document
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RenderTask {
    @Id
    private String id;
    private String blenderVersion;
    private String connectionID;
    private String taskID;
    private String taskFile;
    private String taskFileMD5Sum;
    private String blenderExecutable;
    private String taskDir;
    private String projectName;
    private String projectID;
    private String queueID;
    private String benchmarkURL;
    private Integer taskResolutionX;
    private Integer taskResolutionY;
    private Integer taskResPercentage;
    private Integer taskTileSize;
    private Integer samples;
    private Integer cpuRating;
    private Integer gpuRating;
    private Integer cores;
    private Double partMinX;
    private Double partMaxX;
    private Double partMinY;
    private Double partMaxY;
    private List<String> deviceIDs;
    private ComputeOn computeOn;
    private BlenderEngine blenderEngine;
    private Frame frame;
    private Long renderTime;
    private ImageOutputFormat imageOutputFormat;
    private boolean isBenchmark;
    private boolean cancelRequestReceived;
    private boolean complete;
    private boolean inProgress;
    private boolean useParts;
}
