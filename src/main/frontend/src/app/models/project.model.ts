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
import {User} from "./user.model";
import {ComputeMethod} from "../enums/compute.method.enum";

export class Project {
  renderOn: ComputeMethod;
  sethlansUser: User;
  startFrame: number;
  endFrame: number;
  stepFrame: number;
  samples: number;
  resolutionX: number;
  resolutionY: number;
  resPercentage: number;
  currentPercentage: number;
  partsPerFrame: number;
  started: boolean;
  finished: boolean;
  allImagesProcessed: boolean;
  paused: boolean;
  projectName: string;
  blendFileName: string;
  blendFileLocation: string;
  blenderVersion: string;
  currentFrameThumbnail: string;
  project_uuid: string;
  projectRootDir: string;
  frameFileNames: string[];

}
