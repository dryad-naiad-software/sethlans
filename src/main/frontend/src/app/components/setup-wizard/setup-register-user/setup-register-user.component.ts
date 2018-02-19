import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {User} from "../../../models/user.model";

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

  }

  previousStep() {
    let currentProgress = this.setupFormData.getProgress();
    this.setupFormData.setProgress(currentProgress - 1);
  }

}
