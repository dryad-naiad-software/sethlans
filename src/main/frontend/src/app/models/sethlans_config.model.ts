import {Mode} from "../enums/mode.enum";

export class SethlansConfig {
  httpPort: string;
  sethlansIP: string;
  logFile: string;
  projectDir: string;
  blenderDir: string;
  binDir: string;
  rootDir: string;
  benchmarkDir: string;
  tempDir: string;
  scriptsDir: string;
  cacheDir: string;
  mode: Mode;
}
