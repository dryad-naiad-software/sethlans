import {Component, Input, OnInit} from '@angular/core';
import {Mode} from "../../../enums/mode.enum";
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {Server} from "../../../models/server.model";

@Component({
  selector: 'app-setup-mode',
  templateUrl: './setup-mode.component.html',
  styleUrls: ['./setup-mode.component.scss']
})
export class SetupModeComponent implements OnInit {
  @Input() setupFormData;
  mode: any = Mode;
  selectedMode: Mode;


  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.selectedMode = this.setupFormData.getMode();
  }

  save() {
    this.setupFormDataService.setSethlansMode(this.selectedMode);
    if (this.selectedMode === Mode.SERVER) {
      let server: Server = new Server();
      this.setupFormData.setServer(server);
    }
    this.nextScreen()
  }

  nextScreen() {
    let currentProgress = this.setupFormData.getProgress();
    this.setupFormData.setProgress(currentProgress + 1);

  }

}
