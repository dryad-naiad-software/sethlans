import {Component, OnInit} from '@angular/core';
import {faCheck, faDownload, faFlagCheckered, faPlus, faRectangleList} from "@fortawesome/free-solid-svg-icons";
import {faClipboard} from "@fortawesome/free-regular-svg-icons";
import {Node} from "../../models/system/node.model";
import {SethlansService} from "../../services/sethlans.service";
import {NodeWizardProgress} from "../../enums/nodewizardprogress.enum";


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
  sethlansAPIKey: string = ''

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

  startNodeWizard() {
    this.nodeWizardScreen = true;

  }

  cancelNodeWizard() {
    this.nodeWizardScreen = false;
  }

}
