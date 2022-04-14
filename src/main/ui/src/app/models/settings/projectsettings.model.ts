/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {BlenderEngine} from "../../enums/blenderengine.enum";
import {ComputeOn} from "../../enums/computeon.enum";
import {AnimationType} from "../../enums/animationtype.enum";
import {VideoSettings} from "./videosettings.model";
import {ImageSettings} from "./imagesettings.model";

/**
 * File created by Mario Estrella on 4/13/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */
export class ProjectSettings {
  blenderEngine: BlenderEngine;
  computeOn: ComputeOn;
  animationType: AnimationType;
  startFrame: number;
  endFrame: number;
  stepFrame: number
  samples: number;
  partsPerFrame: number;
  totalNumberOfFrames: number;
  useParts: boolean;
  blendFilename: string;
  blendFilenameMD5Sum: string;
  blenderVersion: string;
  blenderZipFilename: string;
  blenderZipFilenameMD5Sum: string;
  videoSettings: VideoSettings;
  imageSettings: ImageSettings;

  constructor() {
    this.blenderEngine = BlenderEngine.CYCLES;
    this.computeOn = ComputeOn.HYBRID;
    this.animationType = AnimationType.IMAGES;
    this.startFrame = 0;
    this.endFrame = 0;
    this.stepFrame = 0;
    this.samples = 0;
    this.partsPerFrame = 0;
    this.totalNumberOfFrames = 0;
    this.useParts = true;
    this.blendFilename = '';
    this.blendFilenameMD5Sum = '';
    this.blenderVersion = '';
    this.blenderZipFilename = '';
    this.blenderZipFilenameMD5Sum = '';
    this.videoSettings = new VideoSettings();
    this.imageSettings = new ImageSettings();
  }
}
