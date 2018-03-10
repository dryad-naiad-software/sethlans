export class Server {
  blenderVersion: string;

  getBlenderVersion(): string {
    return this.blenderVersion;
  }

  setBlenderVersion(blenderVersion: string) {
    this.blenderVersion = blenderVersion;
  }
}
