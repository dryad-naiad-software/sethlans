import {Mode} from "../../../enums/mode";
import {SetupProgress} from "../../../enums/setupProgress";

export class SetupFormData {
  mode: Mode;
  setupProgress: SetupProgress;


  constructor(setupProgress: SetupProgress) {
    this.setupProgress = setupProgress;
  }
}
