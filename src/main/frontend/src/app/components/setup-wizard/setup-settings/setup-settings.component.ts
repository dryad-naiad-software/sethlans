import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";
import {Mode} from "../../../enums/mode";

@Component({
  selector: 'app-setup-settings',
  templateUrl: './setup-settings.component.html',
  styleUrls: ['./setup-settings.component.scss']
})
export class SetupSettingsComponent implements OnInit {
  @Input() setupFormData;

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.http.get('/api/info/root_directory', {responseType: 'text'})
      .subscribe((rootDirectory: string) => {
        this.setupFormData.setRootDirectory(rootDirectory);
        console.log(this.setupFormData.getRootDirectory());
      });
    this.http.get('/api/info/sethlans_ip', {responseType: 'text'})
      .subscribe((sethlansIP: string) => {
        this.setupFormData.setIPAddress(sethlansIP);
        console.log(this.setupFormData.getIPAddress());
      });
    this.http.get('/api/info/sethlans_port', {responseType: 'text'})
      .subscribe((sethlansPort: any) => {
        this.setupFormData.setSethlansPort(sethlansPort);
        console.log(this.setupFormData.getSethlansPort());
      });
  }

  previousStep() {
    if (this.setupFormData.getMode() === Mode.SERVER) {
      this.setupFormData.setProgress(2);
    }
    else if (this.setupFormData.getMode() === Mode.NODE) {
      this.setupFormData.setProgress(3);
    }
    else if (this.setupFormData.getMode() === Mode.DUAL) {
      this.setupFormData.setProgress(4);
    }
  }

  save() {

  }
}
