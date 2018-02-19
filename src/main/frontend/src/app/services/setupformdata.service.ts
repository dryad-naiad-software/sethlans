import {Injectable} from "@angular/core";
import {SetupFormData} from "../models/setupformdata.model";
import {Mode} from "../enums/mode";

@Injectable()
export class SetupFormDataService {
  private setupFormData: SetupFormData = new SetupFormData();

  getSetupFormData(): SetupFormData {
    return this.setupFormData;
  }

  setSethlansMode(mode: Mode) {
    this.setupFormData.setMode(mode);
  }
}
