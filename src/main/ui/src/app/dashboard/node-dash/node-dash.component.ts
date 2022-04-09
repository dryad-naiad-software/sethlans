import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../../services/sethlans.service";
import {NodeDashboard} from "../../models/views/node-dashboard.model";
import {NodeType} from "../../enums/nodetype.enum";
import {faSquare} from "@fortawesome/free-solid-svg-icons";


@Component({
  selector: 'app-node-dash',
  templateUrl: './node-dash.component.html',
  styleUrls: ['./node-dash.component.css']
})
export class NodeDashComponent implements OnInit {
  nodeDashboard: NodeDashboard | undefined;
  NodeType = NodeType;
  faSquare = faSquare;


  constructor(private sethlansService: SethlansService) {

  }

  ngOnInit(): void {
    this.sethlansService.getNodeDashBoard().subscribe((obj) => {
      this.nodeDashboard = new NodeDashboard(obj);
    })

  }

}
