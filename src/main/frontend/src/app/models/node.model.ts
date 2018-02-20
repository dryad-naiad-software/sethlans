import {ComputeMethod} from "../enums/compute.method";
import {GPU} from "./gpu.model";

export class Node {
  computeMethod: ComputeMethod;
  cores: number;
  selectedGPUs: GPU[];
}
