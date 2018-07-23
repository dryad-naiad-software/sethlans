import {ComputeMethod} from "../enums/compute.method.enum";
import {ProjectType} from "../enums/project_type.enum";
import {BlenderEngine} from "../enums/blender_engine.enum";
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
  outputFormat: any;
  useParts: boolean;
  thumbnailPresent: boolean;
  thumbnailURL: string;
  currentPercentage: number;
  frameRate: string;
}
