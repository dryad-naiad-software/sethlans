import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../service/setupFormData.service";

@Component({
  selector: 'app-setup-register-user',
  templateUrl: './setup-register-user.component.html',
  styleUrls: ['./setup-register-user.component.scss']
})
export class SetupRegisterUserComponent implements OnInit {
  @Input() setupFormData;

  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

}
