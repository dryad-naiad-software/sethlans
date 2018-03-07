import {Component, OnInit} from '@angular/core';
import {Subject} from "rxjs/Subject";
import {NodeInfo} from "../../../models/node_info.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.scss']
})
export class NodesComponent implements OnInit {
  dtTrigger: Subject<any> = new Subject();
  nodeList: NodeInfo[] = [];

  constructor(private modalService: NgbModal) {
  }

  openModal(content) {
    this.modalService.open(content);
  }

  addNode() {
  }

  scanNode() {
  }

  ngOnInit() {
  }

}
