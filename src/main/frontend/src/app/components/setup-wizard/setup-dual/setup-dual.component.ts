import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";

@Component({
  selector: 'app-setup-dual',
  templateUrl: './setup-dual.component.html',
  styleUrls: ['./setup-dual.component.scss']
})
export class SetupDualComponent implements OnInit, AfterViewInit {
  @Input() setupFormData;

  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

  previousStep() {
    this.setupFormData.setProgress(1);
    this.setupFormData.setNode(null);
    this.setupFormData.setServer(null);

  }

  ngAfterViewInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

  save() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.nextStep();
  }

  nextStep() {
    this.setupFormData.setProgress(5);

  }

}
