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

import {NodeType} from "../../enums/nodetype.enum";
import {GPU} from "../hardware/gpu.model";

/**
 * File created by Mario Estrella on 4/4/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */
export class NodeSettings {
  nodeType: NodeType;
  cores: number;
  tileSizeGPU: number;
  tileSizeCPU: number;
  selectedGPUs: Array<GPU>;
  gpuCombined: boolean;
  apiKey: string;

  constructor() {
    this.nodeType = NodeType.CPU_GPU;
    this.cores = 0;
    this.tileSizeGPU = 0;
    this.tileSizeCPU = 0;
    this.selectedGPUs = new Array<GPU>();
    this.gpuCombined = false;
    this.apiKey = '';
  }

  setNodeSettings(obj: any) {
    this.nodeType = obj.nodeType;
    this.cores = obj.cores;
    this.tileSizeGPU = obj.tileSizeGPU;
    this.tileSizeCPU = obj.tileSizeCPU;
    this.selectedGPUs = obj.selectedGPUs;
    this.gpuCombined = obj.gpuCombined;
    this.apiKey = obj.apiKey;
  }
}
