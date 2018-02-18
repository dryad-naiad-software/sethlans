import {Component, Input, OnInit} from '@angular/core';
import {SetupProgress} from "../../../enums/setupProgress";
import {SetupFormDataService} from "../service/setupFormData.service";
import {Mode} from "../../../enums/mode";

@Component({
  selector: 'app-setupnav',
  templateUrl: './setupnav.component.html',
  styleUrls: ['./setupnav.component.scss']
})
export class SetupnavComponent implements OnInit {
  progress: any = SetupProgress;
  @Input() setupFormData;


  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

  nextStep() {
    let currentProgress = this.setupFormDataService.getSetupProgress();
    let currentMode = this.setupFormDataService.getSethlansMode();
    if (currentProgress == 0) {
      this.setupFormData.setProgress(currentProgress + 1);
    }
    if (currentProgress == 1) {
      if (currentMode == Mode.SERVER) {
        this.setupFormData.setProgress(2);
      }
      else if (currentMode == Mode.NODE) {
        this.setupFormData.setProgress(3);
      }
      else if (currentMode == Mode.DUAL) {
        this.setupFormData.setProgress(4);
      }
    }

  }

  previousStep() {
    let currentProgress = this.setupFormDataService.getSetupProgress();
    if (currentProgress == 1) {
      this.setupFormData.setProgress(currentProgress - 1)
    }
    if (currentProgress == 2 || currentProgress == 3 || currentProgress == 4) {
      this.setupFormData.setProgress(1);
    }


  }

}
