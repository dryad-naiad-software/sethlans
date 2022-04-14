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

import {ProjectState} from "../../enums/projectstate.enum";

/**
 * File created by Mario Estrella on 4/13/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */
export class ProjectStatus {
  currentPercentage: number;
  completedFrames: number;
  totalQueueSize: number;
  remainingQueueSize: number;
  renderedQueueItems: number;
  queueIndex: number;
  currentFrame: number;
  currentPart: number;
  totalRenderTime: number;
  totalProjectTime: number;
  timerStart: number;
  timerEnd: number;
  allImagesProcessed: boolean;
  reEncode: boolean;
  projectState: ProjectState;

  constructor() {
    this.currentPercentage = 0;
    this.completedFrames = 0;
    this.totalQueueSize = 0;
    this.remainingQueueSize = 0;
    this.renderedQueueItems = 0;
    this.queueIndex = 0;
    this.currentFrame = 0;
    this.currentPart = 0;
    this.totalRenderTime = 0;
    this.totalProjectTime = 0;
    this.timerStart = 0;
    this.timerEnd = 0;
    this.allImagesProcessed = false;
    this.reEncode = false;
    this.projectState = ProjectState.ADDED;
  }
}
