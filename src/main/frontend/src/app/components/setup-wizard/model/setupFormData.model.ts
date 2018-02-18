import {Mode} from "../../../enums/mode";
import {SetupProgress} from "../../../enums/setupProgress";
import {User} from "../../../models/user.model";

export class SetupFormData {
  private mode: Mode;
  private setupProgress: SetupProgress;
  private user: User;


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

  setUser(user: User) {
    this.user = user;
  }

  getUser(): User {
    return this.user;
  }

}
