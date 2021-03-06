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
import {User} from '../../models/user.model';
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Mode} from '../../enums/mode.enum';
import {UserChallenge} from '../../models/user_challenge.model';

@Component({
  selector: 'app-register-user',
  templateUrl: './register-user.component.html',
  styleUrls: ['./register-user.component.scss']
})
export class RegisterUserComponent implements OnInit {

  logo: any = 'assets/images/logo-dark.png';
  user: User;
  userExists: boolean;
  existingUserName: string;
  modes: any = Mode;
  currentMode: Mode;
  challengeQuestions: string[];
  challenge1: UserChallenge;
  challenge2: UserChallenge;
  challenge3: UserChallenge;
  success: boolean;
  showPass: boolean;
  showResponse1: boolean;
  showResponse2: boolean;
  showResponse3: boolean;

  constructor(private router: Router, private http: HttpClient, private route: ActivatedRoute) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';

    this.challenge1 = new UserChallenge();
    this.challenge2 = new UserChallenge();
    this.challenge3 = new UserChallenge();
    this.showPass = false;
    this.showResponse1 = false;
    this.showResponse2 = false;
    this.showResponse3 = false;
    this.route.queryParams.subscribe(params => {
      this.userExists = params['error'];
      this.existingUserName = params['username'];
      this.success = params['success'];
    });
  }

  ngOnInit() {
    this.http.get('/api/info/challenge_question_list').subscribe((challengeQuestions: string[]) => {
      this.challengeQuestions = challengeQuestions;
      this.challenge1.challenge = this.challengeQuestions[0];
      this.challenge2.challenge = this.challengeQuestions[1];
      this.challenge3.challenge = this.challengeQuestions[2];
    });
    this.user = new User();
    this.http.get('/api/info/sethlans_mode').subscribe((sethlansmode) => this.currentMode = sethlansmode['mode']);

  }

  populateUserSecurityQuestions() {
    this.user.challengeList = [];
    this.user.challengeList.push(this.challenge1);
    this.user.challengeList.push(this.challenge2);
    this.user.challengeList.push(this.challenge3);
    this.user.securityQuestionsSet = true;
  }

  login() {
    window.location.href = '/login';
  }

  submitUser(event, form) {
    if (event.key === 'Enter' && form.valid) {
      this.onSubmit();
    }

  }

  onSubmit() {
    this.populateUserSecurityQuestions();
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/setup/self_register', JSON.stringify(this.user), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        window.location.href = '/register?success=true';
      } else {
        window.location.href = '/register?error=true&username=' + this.user.username;
      }
    });
  }


}
