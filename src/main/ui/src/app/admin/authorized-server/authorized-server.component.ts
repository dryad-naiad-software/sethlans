import {Component, OnInit} from '@angular/core';
import {faKey, faPlus, faServer} from "@fortawesome/free-solid-svg-icons";
import {SethlansService} from "../../services/sethlans.service";
import {Server} from "../../models/system/server.model";


@Component({
  selector: 'app-authorized-server',
  templateUrl: './authorized-server.component.html',
  styleUrls: ['./authorized-server.component.css']
})
export class AuthorizedServerComponent implements OnInit {
  faKey = faKey;
  faPlus = faPlus;
  faServer = faServer;
  server: Server | undefined
  nodeAPIKey: string | undefined
  apiScreenToggle: boolean = false;
  addAPIScreen: boolean = false;

  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getServersOnNode().subscribe((data: any) => {
      this.server = data;
    })
    this.sethlansService.getNodeAPIKey().subscribe((data: any) => {
      this.nodeAPIKey = data.api_key;
    })
  }

  toggleScreen() {
    this.apiScreenToggle = !this.apiScreenToggle;
  }

  clearKey() {
    this.nodeAPIKey = '';
  }

  addAPI() {
    this.addAPIScreen = true;
  }

  setAPI() {
    this.addAPIScreen = false;
  }

}
