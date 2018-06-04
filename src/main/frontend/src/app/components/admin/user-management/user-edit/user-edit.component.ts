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
import {ActivatedRoute} from "@angular/router";
import {UserInfo} from "../../../../models/userinfo.model";
import {Role} from "../../../../enums/role.enum";

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {
  id: number;
  userInfo: UserInfo;
  passFields: PasswordSet;
  requestingUser: UserInfo;
  newEmail: string;
  emailError: boolean;
  passwordError: boolean;
  roleSelection: SelectedRole[];


  constructor(private http: HttpClient, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.getThisUser();
    this.passFields = new PasswordSet();
    this.route.params.subscribe(params => {
      this.id = +params['id'];
      this.loadUser();
    });
  }

  loadUser() {
    this.http.get('/api/management/get_user/' + this.id + '/').subscribe((userInfo: UserInfo) => {
      this.userInfo = userInfo;
      let roles = this.roleSelection;
      userInfo.roles.forEach(function (value: Role) {

      })
    });
  }

  getThisUser() {
    this.http.get('/api/management/requesting_user/').subscribe((userInfo: UserInfo) => {
      this.requestingUser = userInfo;
      if (userInfo.roles.indexOf(Role.ADMINISTRATOR) !== -1 || userInfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
        let roles = [Role.ADMINISTRATOR, Role.USER];
        let roleSelection = this.roleSelection;
        roles.forEach(function (value: Role) {
          let role = SelectedRole(value, false);
          roleSelection.push(role);
        });
      }
      if (userInfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
        let roles = Object.keys(Role);
        let roleSelection = this.roleSelection;
        roles.forEach(function (value: Role) {
          let role = SelectedRole(value, false);
          roleSelection.push(role);
        });
      }

    });
  }

  changeEMail() {
    let emailChange = new HttpParams().set('id', this.id.toString()).set('email', this.newEmail);
    this.http.post('/api/management/change_email/', emailChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      console.log(response);
      if (response) {
        window.location.href = "/admin/user_management/";
      }
      else {
        this.emailError = true;
      }
    });

  }

  changePassword() {
    let passwordChange = new HttpParams().set('id', this.id.toString()).set('passToCheck', this.passFields.currentPass).set('newPassword', this.passFields.newPass);
    this.http.post('/api/management/change_password/', passwordChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      console.log(response);
      if (response) {
        window.location.href = "/admin/user_management/";
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

class SelectedRole {
  role: Role;
  selected: boolean;
}
