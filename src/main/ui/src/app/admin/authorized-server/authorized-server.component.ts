import {Component, OnInit} from '@angular/core';
import {faKey} from "@fortawesome/free-solid-svg-icons";
import {SethlansService} from "../../services/sethlans.service";
import {Server} from "../../models/system/server.model";


@Component({
  selector: 'app-authorized-server',
  templateUrl: './authorized-server.component.html',
  styleUrls: ['./authorized-server.component.css']
})
export class AuthorizedServerComponent implements OnInit {
  faKey = faKey;
  serverList: Array<Server> | undefined
  nodeAPIKey: string | undefined

  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getServersOnNode().subscribe((data: any) => {
      this.serverList = data;
    })
    this.sethlansService.getNodeAPIKey().subscribe((data: any) => {
      this.nodeAPIKey = data.api_key;
      console.log(this.nodeAPIKey)
    })
  }

}
