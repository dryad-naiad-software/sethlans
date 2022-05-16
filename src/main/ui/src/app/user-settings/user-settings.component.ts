import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../services/sethlans.service";
import {UserQuery} from "../models/user/userquery.model";
import {Mode} from "../enums/mode.enum";
import {Role} from "../enums/role.enum";
import {faCheckSquare, faSquare} from '@fortawesome/free-regular-svg-icons';

@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.css']
})
export class UserSettingsComponent implements OnInit {
  user: UserQuery = new UserQuery();
  mode: Mode = Mode.SETUP;
  Mode = Mode;
  faSquare = faSquare;
  faCheckSquare = faCheckSquare;

  isAdministrator = false;
  isSuperAdministrator = false;
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
      if (this.user.roles.indexOf(Role.ADMINISTRATOR) !== -1
        || this.user.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
        this.isAdministrator = true;
      }
      if (this.user.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
        this.isSuperAdministrator = true;
      }
      console.log(this.user)

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
