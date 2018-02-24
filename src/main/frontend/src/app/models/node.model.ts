import {ComputeMethod} from "../enums/compute.method.enum";
import {GPU} from "./gpu.model";

export class Node {
  private computeMethod: ComputeMethod;
  private cores: number;
  private selectedGPUs: GPU[];
  private gpuEmpty: boolean = false;
  private tileSizeCPU: number;
  private tileSizeGPU: number;

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
