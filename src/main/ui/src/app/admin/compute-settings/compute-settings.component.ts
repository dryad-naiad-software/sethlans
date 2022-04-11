import {Component, OnInit} from '@angular/core';
import {faCog} from "@fortawesome/free-solid-svg-icons";
import {NodeDashboard} from "../../models/views/node-dashboard.model";
import {SethlansService} from "../../services/sethlans.service";
import {NodeSettings} from "../../models/settings/nodesettings.model";
import {NodeType} from "../../enums/nodetype.enum";


@Component({
  selector: 'app-compute-settings',
  templateUrl: './compute-settings.component.html',
  styleUrls: ['./compute-settings.component.css']
})
export class ComputeSettingsComponent implements OnInit {
  faCog = faCog;
  nodeDashboard: NodeDashboard = new NodeDashboard();
  changeSettingsScreen: boolean = false;
  nodeSettings: NodeSettings = new NodeSettings();
  NodeType = NodeType;


  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getNodeDashBoard().subscribe((data: any) => {
      this.nodeDashboard.setDashboard(data);
    })

  }

  toggleChangeSettings() {
    this.changeSettingsScreen = !this.changeSettingsScreen;
    this.sethlansService.getNodeSettings().subscribe((data: any) => {
      this.nodeSettings.setNodeSettings(data);
    })
  }

  updateNodeSettings() {
    this.sethlansService.setNodeSettings(this.nodeSettings).subscribe((response: any) => {
      if (response.statusText == "Accepted") {
        this.sethlansService.getNodeDashBoard().subscribe((data: any) => {
          this.nodeDashboard.setDashboard(data);
          this.changeSettingsScreen = false;
        })
      }
    })
  }

}
