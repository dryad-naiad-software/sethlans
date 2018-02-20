import {Injectable} from "@angular/core";
import {SetupFormData} from "../models/setupformdata.model";
import {Mode} from "../enums/mode";
import {User} from "../models/user.model";

@Injectable()
export class SetupFormDataService {
  private setupFormData: SetupFormData = new SetupFormData();

  getSetupFormData(): SetupFormData {
    return this.setupFormData;
  }

  setSethlansMode(mode: Mode) {
    this.setupFormData.setMode(mode);
  }

  setUser(user: User) {
    this.setupFormData.setUser(user);
  }

}
