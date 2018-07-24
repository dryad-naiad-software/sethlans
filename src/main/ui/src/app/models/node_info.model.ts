/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import {ComputeMethod} from '../enums/compute.method.enum';
import {BlenderBinaryOS} from '../enums/blender_binary_os.enum';
import {CPUInfo} from './cpu_info.model';

export class NodeInfo {
  id: number;
  hostname: string;
  ipAddress: string;
  networkPort: string;
  sethlansNodeOS: BlenderBinaryOS;
  computeType: ComputeMethod;
  cpuinfo: CPUInfo;
  selectedCores: string;
  selectedDeviceID: string[];
  selectedGPUModels: string[];
  selectedGPURatings: number[];
  active: boolean;
  disabled: boolean;
  pendingActivation: boolean;
  cpuRating: number;
  benchmarkComplete: boolean;
  rendering: boolean;
  combinedGPURating: number;
  combinedCPUGPURating: number;


  constructor() {
    this.id = 0;
    this.hostname = '';
    this.ipAddress = '';
    this.networkPort = '';
    this.sethlansNodeOS = BlenderBinaryOS.Windows64;
    this.computeType = ComputeMethod.CPU;
    this.cpuinfo = new CPUInfo();
    this.selectedCores = '';
    this.selectedDeviceID = [];
    this.selectedGPUModels = [];
    this.active = false;
    this.disabled = false;
    this.pendingActivation = false;
    this.cpuRating = 0;
    this.benchmarkComplete = false;
    this.combinedCPUGPURating = 0;
    this.combinedGPURating = 0;
    this.selectedGPURatings = [];
  }
}
