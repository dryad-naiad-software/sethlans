/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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
import {UserInfo} from '../../models/user_info.model';
import {Mode} from '../../enums/mode.enum';
import {UserChallenge} from '../../models/user_challenge.model';
import {ActivatedRoute} from '@angular/router';
import {NotificationsForm} from '../../models/forms/notifications_form.model';

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
  status: boolean;
  isNewUser: boolean;
  setPassword: boolean;
  setRecovery: boolean;
  modes: any = Mode;
  currentMode: Mode;
  isAdministrator: boolean;
  challenge1: UserChallenge;
  challenge2: UserChallenge;
  challenge3: UserChallenge;
  challengeQuestions: string[];
  userChallengeToSubmit: UserChallenge[];
  notificationSettings: NotificationsForm;


  constructor(private http: HttpClient, private activatedRoute: ActivatedRoute) {
    this.activatedRoute.queryParams.subscribe(params => {
      let newUser = params['is_new_user'];
      if (newUser != undefined) {
        this.isNewUser = JSON.parse(newUser);
      }
      let status = params['status'];
      if (status != undefined) {
        this.status = JSON.parse(status);
      }
      let needsPassword = params['needs_password_change'];
      if (needsPassword != undefined) {
        this.setPassword = JSON.parse(needsPassword);
      }
      let needsQuestions = params['needs_questions'];
      if (needsQuestions != undefined) {
        this.setRecovery = JSON.parse(needsQuestions);
      }
    });
    this.passFields = new PasswordSet();
    this.userInfo = new UserInfo();
    this.challenge1 = new UserChallenge();
    this.challenge2 = new UserChallenge();
    this.challenge3 = new UserChallenge();
    this.notificationSettings = new NotificationsForm();
  }

  ngOnInit() {
    this.http.get('/api/info/sethlans_mode').subscribe((sethlansmode) => this.currentMode = sethlansmode['mode']);
    this.http.get('/api/users/is_administrator').subscribe((admin: boolean) => {
      this.isAdministrator = admin;
    });
    this.http.get('/api/info/challenge_question_list').subscribe((challengeQuestions: string[]) => {
      this.challengeQuestions = challengeQuestions;
      this.challenge1.challenge = this.challengeQuestions[0];
      this.challenge2.challenge = this.challengeQuestions[1];
      this.challenge3.challenge = this.challengeQuestions[2];
    });
    this.getUserInfo();
    this.passFields = new PasswordSet();
  }

  getUserInfo() {
    this.http.get('/api/users/username')
      .subscribe((user) => {
        let username = user['username'];
        this.http.get('/api/users/get_user/' + username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
          this.notificationSettings.videoEncodingEmailNotifications = this.userInfo.videoEncodingEmailNotifications;
          this.notificationSettings.projectEmailNotifications = this.userInfo.projectEmailNotifications;
          this.notificationSettings.nodeEmailNotifications = this.userInfo.systemEmailNotifications;
          this.notificationSettings.nodeEmailNotifications = this.userInfo.nodeEmailNotifications;
        });
      });
  }

  changeEMail() {
    let emailChange = new HttpParams().set('email', this.newEmail);
    this.http.post('/api/users/change_email/', emailChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings?status=true';
      }
      else {
        window.location.href = '/user_settings?status=false';
      }
    });

  }

  changeNotificationSettings() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/users/change_notifications', JSON.stringify(this.notificationSettings), httpOptions).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings?status=true';
      } else {
        window.location.href = '/user_settings?status=false';
      }
    });
  }

  populateUserSecurityQuestions() {
    this.userChallengeToSubmit = [];
    this.userChallengeToSubmit.push(this.challenge1);
    this.userChallengeToSubmit.push(this.challenge2);
    this.userChallengeToSubmit.push(this.challenge3);
  }

  changeSecurityQuestions() {
    this.populateUserSecurityQuestions();
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/users/change_security_questions', JSON.stringify(this.userChallengeToSubmit), httpOptions).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings?status=true';
      } else {
        window.location.href = '/user_settings?status=false';
      }
    });
  }

  changePassword() {
    let passwordChange = new HttpParams().set('passToCheck', this.passFields.currentPass).set('newPassword', this.passFields.newPass);
    this.http.post('/api/users/change_password/', passwordChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings?status=true';
      }
      else {
        window.location.href = '/user_settings?status=false';
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
