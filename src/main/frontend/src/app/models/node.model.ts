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

import {ComputeMethod} from '../enums/compute.method.enum';
import {GPU} from './gpu.model';

export class Node {
  computeMethod: ComputeMethod;
  cores: number;
  selectedGPUs: GPU[];
  gpuEmpty: boolean = false;
  combined: boolean = true;
  tileSizeCPU: number = 32;
  tileSizeGPU: number = 256;

  setComputeMethod(computeMethod: ComputeMethod) {
    this.computeMethod = computeMethod;
  }

  getComputeMethod(): ComputeMethod {
    return this.computeMethod;
  }

  setCores(cores: number) {
    this.cores = cores;
  }

  getCores(): number {
    return this.cores;
  }

  setSelectedGPUs(selectedGPUs: GPU[]) {
    this.selectedGPUs = selectedGPUs;
  }

  getSelectedGPUs(): GPU[] {
    return this.selectedGPUs;
  }

  setGpuEmpty(gpuEmpty: boolean) {
    this.gpuEmpty = gpuEmpty;
  }

  isGpuEmpty(): boolean {
    return this.gpuEmpty;
  }

  setTileSizeGPU(tileSizeGPU: number) {
    this.tileSizeGPU = tileSizeGPU;
  }

  getTileSizeGPU(): number {
    return this.tileSizeGPU;
  }

  setTileSizeCPU(tileSizeCPU: number) {
    this.tileSizeCPU = tileSizeCPU;
  }

  getTileSizeCPU(): number {
    return this.tileSizeCPU;
  }
}
