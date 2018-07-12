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
      console.log(response);
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
      console.log(response);
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
}
