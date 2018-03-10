import {Component, Input, OnInit} from '@angular/core';
import {Server} from "../../../models/server.model";
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";
import {Mode} from "../../../enums/mode.enum";

@Component({
  selector: 'app-setup-server',
  templateUrl: './setup-server.component.html',
  styleUrls: ['./setup-server.component.scss']
})
export class SetupServerComponent implements OnInit {
  @Input() setupFormData;
  server: Server = new Server();
  mode: any = Mode;
  availableBlenderVersions: string[] = [];

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.setupFormData.setServer(this.server);
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions: any[]) => {
          this.availableBlenderVersions = blenderVersions;
          console.log(this.availableBlenderVersions);
          this.server.setBlenderVersion(this.availableBlenderVersions[0]);
        }, (error) => console.log(error));
  }

  previousStep() {
    this.setupFormData.setServer(null);
    this.setupFormData.setProgress(1);
  }

  save() {
    this.setupFormData.setServer(this.server);
    this.nextStep();
  }

  nextStep() {
    this.setupFormData.setProgress(5);
  }
}
