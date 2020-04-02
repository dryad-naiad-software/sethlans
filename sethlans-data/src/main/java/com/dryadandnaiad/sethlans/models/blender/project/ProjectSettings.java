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

import com.dryadandnaiad.sethlans.enums.AnimationType;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;

/**
 * Created by Mario Estrella on 4/1/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class ProjectSettings {
    private BlenderEngine blenderEngine;
    private ComputeOn computeOn;
    private AnimationType animationType;
    private int startFrame;
    private int endFrame;
    private int stepFrame;
    private int samples;
    private int partsPerFrame;
    private int totalNumberOfFrames;
    private String blendFilename;
    private String blendFilenameMD5Sum;
    private String blendFileLocation;
    private String blenderVersion;
    private VideoSettings videoSettings;
    private ImageSettings imageSettings;
}
