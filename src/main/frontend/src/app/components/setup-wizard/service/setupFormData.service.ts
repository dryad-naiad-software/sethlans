import {Injectable} from "@angular/core";
import {SetupFormData} from "../model/setupFormData.model";
import {Mode} from "../../../enums/mode";
import {SetupProgress} from "../../../enums/setupProgress";
import {User} from "../../../models/user.model";

@Injectable()
export class SetupFormDataService {
  private setupFormData: SetupFormData = new SetupFormData(SetupProgress.START);


  constructor() {
  }

  getSetupFormData(): SetupFormData {
    return this.setupFormData;
  }

  setSethlansMode(mode: Mode) {
    this.setupFormData.setMode(mode);
  }

  getSethlansMode(): Mode {
    return this.setupFormData.getMode();
  }

  setSetupProgress(setupProgres: SetupProgress) {
    this.setupFormData.setProgress(setupProgres);
  }

  getSetupProgress(): SetupProgress {
    return this.setupFormData.getProgress();
  }

  getUser(): User {
    return this.setupFormData.getUser();
  }

  setUser(user: User) {
    return this.setupFormData.setUser(user);
  }


}
