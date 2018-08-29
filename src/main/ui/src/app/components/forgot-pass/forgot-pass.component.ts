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
import {UserChallenge} from '../../models/user_challenge.model';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';

@Component({
  selector: 'app-forgot-pass',
  templateUrl: './forgot-pass.component.html',
  styleUrls: ['./forgot-pass.component.scss']
})
export class ForgotPassComponent implements OnInit {
  username: string;
  retrievedList: UserChallenge[];
  response: string;
  currentProgress: PassResetProgress;
  progress: any = PassResetProgress;
  tokens: string[];
  invalidResponse: boolean;
  password: string;
  passwordConfirm: string;


  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.currentProgress = PassResetProgress.START;
    this.invalidResponse = false;
    this.password = '';
    this.passwordConfirm = '';
  }

  ngOnInit() {
  }

  submitResponse() {
    let answer;
    switch (this.currentProgress) {
      case PassResetProgress.QUESTION1:
        answer = new HttpParams().set('username', this.username).set('key', this.retrievedList[0].response).set('submittedResponse', this.response);
        break;
      case PassResetProgress.QUESTION2:
        answer = new HttpParams().set('username', this.username).set('key', this.retrievedList[1].response).set('submittedResponse', this.response);
        break;
      case PassResetProgress.QUESTION3:
        answer = new HttpParams().set('username', this.username).set('key', this.retrievedList[2].response).set('submittedResponse', this.response);
        break;
    }

    this.http.post('/api/users/submit_challenge_response', answer, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
      , responseType: 'text'
    }).subscribe((token: string) => {
      if (token !== 'invalid') {
        this.tokens.push(token);
        this.invalidResponse = false;
        switch (this.currentProgress) {
          case PassResetProgress.QUESTION1:
            this.currentProgress = PassResetProgress.QUESTION2;
            this.response = '';
            break;
          case PassResetProgress.QUESTION2:
            this.currentProgress = PassResetProgress.QUESTION3;
            this.response = '';
            break;
          case PassResetProgress.QUESTION3:
            this.response = '';
            console.log(this.tokens);
            this.currentProgress = PassResetProgress.CHANGE_PASS;
            break;
        }
      } else {
        this.invalidResponse = true;
      }
    });

  }

  submitPassChange() {
    console.log('Submitting new password');
    let passwordChange = new HttpParams().set('username', this.username).set('tokens[0]', this.tokens[0]).set('tokens[1]', this.tokens[1]).set('tokens[2]', this.tokens[2]).set('newPassword', this.password);
    this.http.post('/api/users/reset_password', passwordChange, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((sentResponse: boolean) => {
      console.log(sentResponse);
    });
  }

  login() {
    window.location.href = '/login';
  }

  retrieveQuestions() {
    this.http.get('/api/users/user_challenge_list' + '?username=' + this.username).subscribe((userQuestions: UserChallenge[]) => {
      this.retrievedList = userQuestions;
      this.tokens = [];
      this.currentProgress = PassResetProgress.QUESTION1;

    });

  }

  next() {
    switch (this.currentProgress) {
      case PassResetProgress.START:
        this.retrieveQuestions();
        break;
      case PassResetProgress.QUESTION1:
        this.submitResponse();
        break;
      case PassResetProgress.QUESTION2:
        this.submitResponse();
        break;
      case PassResetProgress.QUESTION3:
        this.submitResponse();
        break;
      case PassResetProgress.CHANGE_PASS:
        this.submitPassChange();
        break;

    }

  }

}

enum PassResetProgress {
  START,
  QUESTION1,
  QUESTION2,
  QUESTION3,
  CHANGE_PASS
}
