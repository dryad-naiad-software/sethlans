import {Component, Input, OnInit} from '@angular/core';
import {Title} from "@angular/platform-browser";
import {SetupFormDataService} from "./service/setupFormData.service";
import {SetupProgress} from "../../enums/setupProgress";

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.scss']
})
export class SetupWizardComponent implements OnInit {
  title = "Sethlans Setup Wizard";
  progress: any = SetupProgress;
  @Input() setupFormData;

  constructor(private titleService: Title, private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.titleService.setTitle(this.title);
    this.setupFormData = this.setupFormDataService.getSetupFormData();

  }

}
