import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {SethlansConfig} from "../../../models/sethlans_config.model";
import {Mode} from "../../../enums/mode.enum";

@Component({
  selector: 'app-sethlans-settings',
  templateUrl: './sethlans-settings.component.html',
  styleUrls: ['./sethlans-settings.component.scss']
})
export class SethlansSettingsComponent implements OnInit {
  sethlansConfig: SethlansConfig;
  mode: any = Mode;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/management/current_settings').subscribe((sethlansConfig: SethlansConfig) => {
      this.sethlansConfig = sethlansConfig;
      console.log(this.sethlansConfig);
    });
  }

}
