import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {User} from "../../../models/user.model";
import {Mode} from "../../../enums/mode";

@Component({
  selector: 'app-setup-register-user',
  templateUrl: './setup-register-user.component.html',
  styleUrls: ['./setup-register-user.component.scss']
})
export class SetupRegisterUserComponent implements OnInit {
  @Input() setupFormData;
  user: User;

  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.user = this.setupFormData.getUser();
  }

  save() {
    this.setupFormDataService.setUser(this.user);
    this.nextStep();

  }

  previousStep() {
    let currentProgress = this.setupFormData.getProgress();
    this.setupFormData.setProgress(currentProgress - 1);
    // this.user.setPassword('');
    // this.user.setPasswordConfirm('');
  }

  nextStep() {
    let currentMode = this.setupFormData.getMode();
    if (currentMode === Mode.SERVER) {
      this.setupFormData.setProgress(2);
    }
    if (currentMode === Mode.NODE) {
      this.setupFormData.setProgress(3);
    }

    if (currentMode === Mode.DUAL) {
      this.setupFormData.setProgress(4);
    }

  }

  userSubmit(event, form: any) {
    if (event.key === "Enter" && form.valid) {
      this.save();
    }

  }
}
