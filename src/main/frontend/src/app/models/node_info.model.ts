import {ComputeMethod} from "../enums/compute.method.enum";
import {BlenderBinaryOS} from "../enums/blenderbinaryos.enum";

export class NodeInfo {
  nodeStatus: string;
  hostname: string;
  ipAddress: string;
  networkPort: string;
  sethlansNodeOS: BlenderBinaryOS;
  computeType: ComputeMethod;
  cpuName: string;
  selectedCores: string;
  selectedGPUModels: string[];
  benchmarkRating: number;

}
