import {Mode} from "../../../enums/mode";
import {SetupProgress} from "../../../enums/setupProgress";

export class SetupFormData {
  private mode: Mode;
  private setupProgress: SetupProgress;


  constructor(setupProgress: SetupProgress) {
    this.setupProgress = setupProgress;
  }

  setMode(mode: Mode) {
    this.mode = mode;
  }

  getMode(): Mode {
    return this.mode;
  }

  setProgress(setupProgress: SetupProgress) {
    this.setupProgress = setupProgress;
  }

  getProgress(): SetupProgress {
    return this.setupProgress;
  }

}
