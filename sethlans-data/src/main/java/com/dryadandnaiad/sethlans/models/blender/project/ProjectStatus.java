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

package com.dryadandnaiad.sethlans.models.blender.project;

import com.dryadandnaiad.sethlans.enums.ProjectState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Mario Estrella on 4/1/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectStatus {
    private int currentPercentage;
    private int completedFrames;
    private int totalQueueSize;
    private int remainingQueueSize;
    private int queueIndex;
    private Long totalRenderTime;
    private Long totalProjectTime;
    private Long timerStart;
    private Long timerEnd;
    private boolean allImagesProcessed;
    private boolean userStopped;
    private boolean queueFillComplete;
    private boolean reEncode;
    private String currentFrameThumbnail;
    private ProjectState projectState;
}