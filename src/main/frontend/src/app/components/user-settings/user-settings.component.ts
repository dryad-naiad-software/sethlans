/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {UserInfo} from "../../models/userinfo.model";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";


@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {
  username: string;
  userInfo: UserInfo;
  changePass = false;
  passFields: PasswordSet;
  formValid: boolean;

  constructor(private http: HttpClient, private router: Router) {
    this.passFields = new PasswordSet();
    this.userInfo = new UserInfo();
  }

  submitSettingChanges(event, form) {
    if (event.key === "Enter" && form.valid) {
      this.saveChanges(form);
    }

  }

  ngOnInit() {
    this.getUserInfo();
    let timer = Observable.timer(5000, 2000);
    timer.subscribe(() => {
      this.getUserInfo()
    });
  }

  getUserInfo() {
    this.http.get('/api/users/username', {responseType: 'text'})
      .subscribe((user: string) => {
        this.username = user;
        this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
          console.log(this.userInfo);
        });
      });
  }

  cancel() {
    window.location.href = "/";
  }

  saveChanges(form) {
    if (form.valid) {
      if (!this.changePass) {
        console.log("Change pass false");
        this.changeEmail();
      }


      if (this.changePass) {
        console.log("Change pass true");
        this.changeEmailAndPassword();
      }
    }

  }

  changeEmail() {
    let emailChange = new HttpParams().set('username', this.username).set('newEmail', this.userInfo.email);
    this.http.post('/api/users/email_change', emailChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      console.log(response);
      if (response) {
        this.formValid = true;

      } else if (!response) {
        this.formValid = false;

      }
    });
  }


  changeEmailAndPassword() {
    let emailAndPassChange = new HttpParams().set('username', this.username).set('newEmail', this.userInfo.email).set('passToCheck', this.passFields.currentPass).set('newPassword', this.passFields.newPass);
    this.http.post('/api/users/email_pass_change', emailAndPassChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((passwordResponse: boolean) => {
      console.log(passwordResponse);
      if (passwordResponse) {
        this.formValid = true;
      } else if (!passwordResponse) {
        this.formValid = false;

      }
    });
  }
}

class PasswordSet {
  currentPass: string;
  newPass: string;
  newPassConfirm: string;
}
