import {Component, Input, OnInit} from '@angular/core';
import {Mode} from "../../../enums/mode";
import {SetupFormDataService} from "../../../services/setupformdata.service";

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
  }

  save(form: any) {
    this.setupFormDataService.setSethlansMode(this.selectedMode);
    this.nextScreen()
  }

  nextScreen() {
    let currentProgress = this.setupFormData.getProgress();
    this.setupFormData.setProgress(currentProgress + 1);

  }

}
