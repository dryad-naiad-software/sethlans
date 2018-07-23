import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {UserInfo} from '../../models/userinfo.model';

@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {
  id: number;
  userInfo: UserInfo;
  passFields: PasswordSet;
  newEmail: string;
  emailError: boolean;
  passwordError: boolean;

  constructor(private http: HttpClient) {
    this.passFields = new PasswordSet();
    this.userInfo = new UserInfo();
  }



  ngOnInit() {
    this.getUserInfo();
    this.passFields = new PasswordSet();
  }

  getUserInfo() {
    this.http.get('/api/users/username')
      .subscribe((user) => {
        let username = user['username'];
        this.http.get('/api/users/get_user/' + username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
        });
      });
  }

  changeEMail() {
    let emailChange = new HttpParams().set('email', this.newEmail);
    this.http.post('/api/users/change_email/', emailChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings/';
      }
      else {
        this.emailError = true;
      }
    });

  }

  changePassword() {
    let passwordChange = new HttpParams().set('passToCheck', this.passFields.currentPass).set('newPassword', this.passFields.newPass);
    this.http.post('/api/users/change_password/', passwordChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings/';
      }
      else {
        this.passwordError = true;
      }
    });
  }

}

class PasswordSet {
  currentPass: string;
  newPass: string;
  newPassConfirm: string;

  constructor() {
    this.currentPass = "";
    this.newPass = "";
    this.newPassConfirm = "";
  }
}
