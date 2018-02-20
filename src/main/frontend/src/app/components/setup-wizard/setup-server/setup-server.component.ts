import {Component, Input, OnInit} from '@angular/core';
import {Server} from "../../../models/server.model";
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-setup-server',
  templateUrl: './setup-server.component.html',
  styleUrls: ['./setup-server.component.scss']
})
export class SetupServerComponent implements OnInit {
  @Input() setupFormData;
  server: Server = new Server();
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
          this.server.blenderVersion = [blenderVersions[0]]
        }, (error) => console.log(error));
  }

  previousStep() {
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
