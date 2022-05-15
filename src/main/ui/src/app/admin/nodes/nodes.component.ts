import {Component, OnInit} from '@angular/core';
import {faCheck, faDownload, faFlagCheckered, faPlus, faRectangleList} from "@fortawesome/free-solid-svg-icons";
import {faClipboard} from "@fortawesome/free-regular-svg-icons";
import {Node} from "../../models/system/node.model";
import {SethlansService} from "../../services/sethlans.service";
import {NodeWizardProgress} from "../../enums/nodewizardprogress.enum";
import {NodeWizardType} from "../../enums/nodewizardtype.enum";
import {NodeForm} from "../../models/forms/node-form.model";
import {Clipboard} from "@angular/cdk/clipboard";
import {BehaviorSubject} from "rxjs";


@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.css']
})
export class NodesComponent implements OnInit {
  faPlus = faPlus;
  faDownload = faDownload;
  faCheck = faCheck;
  faFlagCheckered = faFlagCheckered;
  faClipboard = faClipboard;
  faRectangleList = faRectangleList;
  nodeWizardScreen = false;
  nodeWizardProgress: NodeWizardProgress = NodeWizardProgress.START;
  NodeWizardProgress = NodeWizardProgress;
  sethlansAPIKey: string = '';
  nodeWizardType: NodeWizardType = NodeWizardType.SCAN;
  NodeWizardType = NodeWizardType;
  manualNode = new NodeForm();
  addDisabled: boolean = true;
  manualNodeFormList = new Array<NodeForm>();
  nodeFormList = new Array<NodeForm>();
  networkNodeList = new Array<Node>();
  scanning: boolean = false;
  selectedNodeList = new Array<Node>();
  nextDisabled: boolean = false;
  submitDisabled: boolean = false;
  nodeDataSource = new BehaviorSubject<any[]>([]);
  nodeDisplayedColumns = new BehaviorSubject<string[]>([
    'status',
    'hostname',
    'ipAddress',
    'port',
    'os',
    'compute',
    'cpu',
    'cores',
    'gpus',
    'slots',
    'benchmarkStatus',
    'actions'
  ]);
  nodeListRetrieved: boolean = false;


  constructor(private sethlansService: SethlansService, private clipboard: Clipboard) {
  }

  ngOnInit(): void {
    this.getCurrentNodes()
    this.sethlansService.getServerAPIKey().subscribe((data: any) => {
      this.sethlansAPIKey = data.api_key;
    })

  }

  getCurrentNodes() {
    this.sethlansService.getCurrentNodeList().subscribe((data: any) => {
      this.nodeDataSource = new BehaviorSubject<any[]>(data);
    })
    setTimeout(() => {
      this.getCurrentNodes();
    }, 15000);
  }

  retrieveNodes() {
    this.sethlansService.retrieveNetworkNodeList(this.manualNodeFormList).subscribe((response: any) => {
      if (response.statusText == "Accepted") {
        this.nodeListRetrieved = true;
        this.selectedNodeList = response.body;
      }
    })
  }

  getNetworkNodeList() {
    this.sethlansService.networkNodeScan().subscribe((data: any) => {
      this.networkNodeList = data;
      this.scanning = false;
    })

  }

  copyAPIKey() {
    this.clipboard.copy(this.sethlansAPIKey)

  }

  checkManualNode() {
    if (this.manualNode.ipAddress != '' && this.manualNode.networkPort != '') {
      this.addDisabled = false;
    }
    if (this.manualNode.ipAddress == null || this.manualNode.networkPort == null) {
      this.addDisabled = true;
    }
  }

  manualAddNodeToList() {
    this.manualNodeFormList.push(this.manualNode);
    this.manualNode = new NodeForm();
  }

  addNode(node: Node, $event: MouseEvent) {
    ($event.target as HTMLButtonElement).disabled = true;
    this.selectedNodeList.push(node);
    if (this.selectedNodeList.length > 0) {
      this.nextDisabled = false;
    }
  }

  removeNode(node: Node) {
    this.selectedNodeList = this.selectedNodeList.filter(function (obj) {
      return obj.systemID !== node.systemID;
    })

  }

  startNodeWizard() {
    this.nodeWizardScreen = true;

  }

  cancelNodeWizard() {
    window.location.href = '/admin/nodes';
  }

  next() {
    switch (this.nodeWizardProgress) {
      case NodeWizardProgress.START:
        this.nodeWizardProgress = NodeWizardProgress.ADD;
        if (this.nodeWizardType == NodeWizardType.SCAN) {
          this.scanning = true;
          this.nextDisabled = true;
          this.getNetworkNodeList();
        }
        break;
      case NodeWizardProgress.ADD:
        if (this.nodeWizardType == NodeWizardType.MANUAL) {
          this.retrieveNodes();
        }
        this.nodeWizardProgress = NodeWizardProgress.SUMMARY;
        break;
    }
  }

  previous() {
    switch (this.nodeWizardProgress) {
      case NodeWizardProgress.ADD:
        this.nodeWizardProgress = NodeWizardProgress.START
        break;
      case NodeWizardProgress.SUMMARY:
        this.nodeWizardProgress = NodeWizardProgress.ADD
        if (this.nodeWizardType == NodeWizardType.SCAN) {
          this.scanning = true;
          this.selectedNodeList = new Array<Node>();
          this.getNetworkNodeList();
        }
        if (this.nodeWizardType == NodeWizardType.MANUAL) {
          this.nodeListRetrieved = false;
        }
        break;
    }
  }

  submitNodes() {
    this.submitDisabled = true;
    this.selectedNodeList.forEach((node) => {
      let nodeForm = new NodeForm();
      nodeForm.ipAddress = node.ipAddress;
      nodeForm.networkPort = node.networkPort;
      this.nodeFormList.push(nodeForm);
    })
    this.nodeWizardProgress = NodeWizardProgress.FINISHED
    this.sethlansService.addNodesToServer(this.nodeFormList).subscribe((response) => {
      if (response.statusText == "Created") {
        window.location.href = '/admin/nodes';
      }
    })


  }


}
