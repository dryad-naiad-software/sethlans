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

import {OS} from "../../enums/os.enum";
import {NodeType} from "../../enums/nodetype.enum";
import {GPU} from "../hardware/gpu.model";
import {CPU} from "../hardware/cpu.model";

/**
 * File created by Mario Estrella on 4/11/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class Node {
  hostname: string;
  ipAddress: string;
  networkPort: string;
  systemID: string;
  os: OS;
  nodeType: NodeType;
  active: boolean;
  benchmarkPending: boolean;
  benchmarkComplete: boolean;
  totalRenderingSlots: number;
  cpuRating: number;
  selectedGPUs: Array<GPU>
  cpu: CPU;
  selectedCores: number;

  constructor() {
    this.hostname = '';
    this.ipAddress = '';
    this.networkPort = '';
    this.systemID = '';
    this.os = OS.UNSUPPORTED;
    this.nodeType = NodeType.CPU_GPU;
    this.active = false;
    this.benchmarkPending = false;
    this.benchmarkComplete = false;
    this.cpuRating = 0;
    this.totalRenderingSlots = 0;
    this.selectedGPUs = new Array<GPU>();
    this.cpu = new CPU();
    this.selectedCores = 0;
  }

  setNode(obj: any) {
    this.hostname = obj.hostname;
    this.ipAddress = obj.ipAddress;
    this.networkPort = obj.networkPort;
    this.systemID = obj.systemID;
    this.os = obj.os;
    this.nodeType = obj.nodeType;
    this.active = obj.active;
    this.benchmarkPending = obj.benchmarkPending;
    this.benchmarkComplete = obj.benchmarkComplete;
    this.cpuRating = obj.cpuRating;
    this.totalRenderingSlots = obj.totalRenderingSlots;
    this.selectedGPUs = obj.selectedGPUs;
    this.cpu = obj.cpu;
    this.selectedCores = obj.selectedCores;

  }
}
