import {Component, OnInit} from '@angular/core';
import {faDownload, faPlus} from "@fortawesome/free-solid-svg-icons";
import {Node} from "../../models/system/node.model";
import {SethlansService} from "../../services/sethlans.service";


@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.css']
})
export class NodesComponent implements OnInit {
  faPlus = faPlus;
  faDownload = faDownload;
  nodeList = new Array<Node>();

  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getCurrentNodeList().subscribe((data: any) => {
      this.nodeList = data;
    })
  }

}
