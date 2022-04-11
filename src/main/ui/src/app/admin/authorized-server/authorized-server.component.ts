import {Component, OnInit} from '@angular/core';
import {faKey, faPlus, faServer} from "@fortawesome/free-solid-svg-icons";
import {SethlansService} from "../../services/sethlans.service";
import {Server} from "../../models/system/server.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Mode} from "../../enums/mode.enum";


@Component({
  selector: 'app-authorized-server',
  templateUrl: './authorized-server.component.html',
  styleUrls: ['./authorized-server.component.css']
})
export class AuthorizedServerComponent implements OnInit {
  faKey = faKey;
  faPlus = faPlus;
  faServer = faServer;
  server: Server = new Server();
  mode: Mode = Mode.NODE;
  Mode = Mode;
  nodeAPIKey: string = '';
  apiScreenToggle: boolean = false;
  addAPIScreen: boolean = false;

  constructor(private modalService: NgbModal, private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getAuthorizedServer().subscribe((data: any) => {
      this.server.setServer(data);
    })
    this.sethlansService.getNodeAPIKey().subscribe((data: any) => {
      this.nodeAPIKey = data.api_key;
    })
    this.sethlansService.mode().subscribe((data: any) => {
      this.mode = data.mode;
    })
  }

  open(content: any) {
    this.modalService.open(content, {ariaLabelledBy: 'clearKeyConfirm'}).result.then((result) => {
      console.log(result);
    });
  }

  toggleScreen() {
    this.apiScreenToggle = !this.apiScreenToggle;
  }

  clearKeyModal(content: any) {
    this.modalService.open(content)
  }

  clearAPIKey() {
    this.nodeAPIKey = '';
    this.sethlansService.setNodeAPIKey(this.nodeAPIKey).subscribe((data: any) => {
    })

  }

  addAPI() {
    this.addAPIScreen = true;
  }

  setAPI() {
    this.addAPIScreen = false;
    this.sethlansService.setNodeAPIKey(this.nodeAPIKey).subscribe((data: any) => {
    })
  }

}
