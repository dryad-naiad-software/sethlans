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

/**
 * File created by Mario Estrella on 4/8/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class NodeDashboard {
  nodeType: NodeType
  cpuName: string;
  totalMemory: string;
  selectedCores: number;
  freeSpace: number;
  totalSpace: number;
  usedSpace: number;
  selectedGPUModels: Array<string>
  availableGPUModels: Array<string>
  totalSlots: number;
  gpuCombined: boolean;
  apiKeyPresent: boolean;

  constructor() {
    this.nodeType = NodeType.CPU_GPU;
    this.cpuName = "";
    this.totalMemory = "";
    this.selectedCores = 0;
    this.freeSpace = 0;
    this.totalSpace = 0;
    this.usedSpace = 0;
    this.selectedGPUModels = new Array<string>();
    this.availableGPUModels = new Array<string>();
    this.totalSlots = 0;
    this.gpuCombined = false;
    this.apiKeyPresent = false;
  }

  setDashboard(obj: any) {
    this.nodeType = obj.nodeType;
    this.cpuName = obj.cpuName;
    this.totalMemory = obj.totalMemory;
    this.selectedCores = obj.selectedCores;
    this.freeSpace = obj.freeSpace;
    this.totalSpace = obj.totalSpace;
    this.usedSpace = obj.usedSpace;
    this.selectedGPUModels = obj.selectedGPUModels;
    this.availableGPUModels = obj.availableGPUModels;
    this.totalSlots = obj.totalSlots;
    this.gpuCombined = obj.gpuCombined;
    this.apiKeyPresent = obj.apiKeyPresent;
  }
}
