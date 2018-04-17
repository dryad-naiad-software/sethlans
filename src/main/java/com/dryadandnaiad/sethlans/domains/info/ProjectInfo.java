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

package com.dryadandnaiad.sethlans.domains.info;

import com.dryadandnaiad.sethlans.enums.*;
import lombok.Data;

/**
 * Created Mario Estrella on 3/12/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
public class ProjectInfo {
    private Long id;
    private String projectName;
    private String selectedBlenderversion;
    private ProjectType projectType;
    private ComputeType renderOn;
    private BlenderEngine blenderEngine;
    private int resolutionX;
    private int resolutionY;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int samples;
    private int resPercentage;
    private int partsPerFrame;
    private String username;
    private ProjectStatus projectStatus;
    private boolean allImagesProcessed;
    private RenderOutputFormat outputFormat;
    private boolean useParts;
    private boolean thumbnailPresent;
    private String thumbnailURL;
    private int currentPercentage;
    private String frameRate;
    private String totalRenderTime;
    private String projectTime;
}
