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
import {ComputeMethod} from "../enums/compute.method.enum";
import {ProjectType} from "../enums/project_type.enum";
import {BlenderEngine} from "../enums/blender_engine.enum";
import {RenderOutputFormat} from "../enums/render_output_format.enum";
import {ProjectStatus} from "../enums/project_status.enum";

export class Project {
  id: number;
  projectName: string;
  projectType: ProjectType;
  projectStatus: ProjectStatus;
  renderOn: ComputeMethod;
  resolutionX: number;
  resolutionY: number;
  username: string;
  blenderEngine: BlenderEngine;
  startFrame: number;
  stepFrame: number;
  endFrame: number;
  samples: number;
  resPercentage: number;
  uploadedFile: string;
  uuid: string;
  selectedBlenderversion: string;
  partsPerFrame: number;
  fileLocation: string;
  outputFormat: RenderOutputFormat;
  useParts: boolean;


}
