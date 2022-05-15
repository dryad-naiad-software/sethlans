import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../services/sethlans.service";
import {UserQuery} from "../models/user/userquery.model";
import {Mode} from "../enums/mode.enum";

@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.css']
})
export class UserSettingsComponent implements OnInit {
  user: UserQuery = new UserQuery();
  mode: Mode = Mode.SETUP;
  Mode = Mode;
  newEmail: string = "";
  emailError: boolean = false;
  emailUpdated: boolean = false;
  emailRegEx = new RegExp('^[^\\s@]+@[^\\s@]+\\.[^\\s@]{1,}$');


  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.getCurrentUser().subscribe((data: any) => {
      this.user.setUserQuery(data);
      this.newEmail = this.user.email;
    });
    this.sethlansService.mode().subscribe((data: any) => {
      this.mode = data.mode;
    });
  }

  validateEmail() {
    this.emailError = !this.newEmail.match(this.emailRegEx);
    this.emailUpdated = !this.emailError && this.newEmail !== this.user.email;
  }

  changeEmail() {

  }

}
