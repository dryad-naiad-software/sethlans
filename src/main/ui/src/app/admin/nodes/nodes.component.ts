import {Component, OnInit} from '@angular/core';
import {faCheck, faDownload, faFlagCheckered, faPlus, faRectangleList} from "@fortawesome/free-solid-svg-icons";
import {faClipboard} from "@fortawesome/free-regular-svg-icons";
import {Node} from "../../models/system/node.model";
import {SethlansService} from "../../services/sethlans.service";
import {NodeWizardProgress} from "../../enums/nodewizardprogress.enum";
import {NodeWizardType} from "../../enums/nodewizardtype.enum";
import {NodeForm} from "../../models/forms/node-form.model";


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
  nodeList = new Array<Node>();
  nodeWizardScreen = false;
  nodeWizardProgress: NodeWizardProgress = NodeWizardProgress.START;
  NodeWizardProgress = NodeWizardProgress;
  sethlansAPIKey: string = '';
  nodeWizardType: NodeWizardType = NodeWizardType.SCAN;
  NodeWizardType = NodeWizardType;
  nodeFormList = new Array<NodeForm>();
  nodeForm = new NodeForm();
  networkNodeList = new Array<Node>();
  scanning: boolean = false;


  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getCurrentNodeList().subscribe((data: any) => {
      this.nodeList = data;
    })
    this.sethlansService.getServerAPIKey().subscribe((data: any) => {
      this.sethlansAPIKey = data.api_key;
    })
  }

  getNetworkNodeList() {
    this.sethlansService.networkNodeScan().subscribe((data: any) => {
      this.networkNodeList = data;
      this.scanning = false;
    })

  }

  startNodeWizard() {
    this.nodeWizardScreen = true;

  }

  cancelNodeWizard() {
    this.nodeWizardScreen = false;
  }

  next() {
    if (this.nodeWizardProgress == NodeWizardProgress.START) {
      this.nodeWizardProgress = NodeWizardProgress.ADD;
      if (this.nodeWizardType == NodeWizardType.SCAN) {
        this.scanning = true;
        this.getNetworkNodeList();
      }
    }
  }

  previous() {
    if (this.nodeWizardProgress == NodeWizardProgress.ADD) {
      this.nodeWizardProgress = NodeWizardProgress.START;
    }
  }


}
