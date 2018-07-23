import {ComputeMethod} from "../enums/compute.method.enum";

export class NodeDashboard {
  computeType: ComputeMethod;
  cpuName: string;
  totalMemory: string;
  selectedCores: string;
  freeSpace: number;
  totalSpace: number;
  usedSpace: number;
  selectedGPUModels: string[];
  availableGPUModels: string[];
  totalSlots: number;
  gpuCombined: boolean;

  constructor() {
    this.computeType = ComputeMethod.CPU;
    this.cpuName = "";
    this.totalMemory = "";
    this.selectedCores = "";
    this.freeSpace = 0;
    this.totalSpace = 0;
    this.usedSpace = 0;
    this.selectedGPUModels = [];
    this.availableGPUModels = [];
    this.totalSlots = 0;
    this.gpuCombined = false;
  }
}
