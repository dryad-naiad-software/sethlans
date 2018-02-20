import {Mode} from "../enums/mode";
import {SetupProgress} from "../enums/setupProgress";
import {User} from "./user.model";
import {Server} from "./server.model";
import {Node} from "./node.model";

export class SetupFormData {
  private mode: Mode;
  private setupProgress: SetupProgress;
  private user: User;
  private server: Server;
  private node: Node;

  constructor() {
    this.setupProgress = SetupProgress.START;
    this.mode = Mode.SERVER;
    this.user = new User();
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

  setServer(server: Server) {
    this.server = server;
  }

  getServer(): Server {
    return this.server;
  }

  setNode(node: Node) {
    this.node = node;
  }

  getNode(): Node {
    return this.node;
  }

}
