import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../services/setupformdata.service";
import {SetupProgress} from "../../enums/setupProgress";

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.scss']
})
export class SetupWizardComponent implements OnInit {
  @Input() setupFormData;
  progress: any = SetupProgress;

  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

}
