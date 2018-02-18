import {Injectable} from "@angular/core";
import {SetupFormData} from "../model/setupFormData.model";
import {Mode} from "../../../enums/mode";
import {SetupProgress} from "../../../enums/setupProgress";

@Injectable()
export class SetupFormDataService {
  private setupFormData: SetupFormData = new SetupFormData(SetupProgress.START);


  constructor() {
  }

  getSetupFormData(): SetupFormData {
    return this.setupFormData;
  }

  setSethlansMode(mode: Mode) {
    this.setupFormData.mode = mode;
  }

  getSethlansMode(): Mode {
    return this.setupFormData.mode;
  }

  setSetupProgress(setupProgres: SetupProgress) {
    this.setupFormData.setupProgress = setupProgres;
  }

  getSetupProgress(): SetupProgress {
    return this.setupFormData.setupProgress;
  }


}
