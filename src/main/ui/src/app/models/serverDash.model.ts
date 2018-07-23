export class ServerDashboard {
  totalNodes: number;
  activeNodes: number;
  inactiveNodes: number;
  disabledNodes: number;
  totalSlots: number;
  cpuName: string;
  totalMemory: string;
  freeSpace: number;
  totalSpace: number;
  usedSpace: number;
  numberOfActiveNodesArray: number[];


  constructor() {
    this.activeNodes = 0;
    this.cpuName = "";
    this.disabledNodes = 0;
    this.totalMemory = "";
    this.freeSpace = 0;
    this.usedSpace = 0;
    this.numberOfActiveNodesArray = [];
    this.totalSlots = 0;
    this.totalSpace = 0;
    this.inactiveNodes = 0;
    this.totalNodes = 0;
  }
}
