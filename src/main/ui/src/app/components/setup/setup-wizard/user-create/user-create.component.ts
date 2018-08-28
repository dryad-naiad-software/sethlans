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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SetupWizardForm} from '../../../../models/forms/setup_wizard_form.model';
import {User} from '../../../../models/user.model';
import {UserChallenge} from '../../../../models/user_challenge.model';
import {Mode} from '../../../../enums/mode.enum';

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.scss']
})
export class UserCreateComponent implements OnInit {
  @Input() setupForm: SetupWizardForm;
  @Output() disableNext = new EventEmitter();
  @Output() submitUser = new EventEmitter();
  passwordMatch: boolean = false;
  @Input() challengeQuestions: string[];
  challenge1: UserChallenge;
  challenge2: UserChallenge;
  challenge3: UserChallenge;
  modes: any = Mode;


  constructor() {
    this.challenge1 = new UserChallenge();
    this.challenge2 = new UserChallenge();
    this.challenge3 = new UserChallenge();

  }

  ngOnInit() {
    this.challenge1.challenge = this.challengeQuestions[0];
    this.challenge2.challenge = this.challengeQuestions[1];
    this.challenge3.challenge = this.challengeQuestions[2];
    if (this.setupForm.user == null) {
      this.setupForm.user = new User();
    }
    if (this.setupForm.user.active == false) {
      this.disableNext.emit(true);
    }
  }

  validateAndSubmit(event, userForm) {
    this.populateUserSecurityQuestions();
    this.passwordMatch = this.setupForm.user.password === this.setupForm.user.passwordConfirm;
    if (userForm.valid && this.passwordMatch) {
      this.disableNext.emit(false);
      if (event.key === 'Enter') {
        this.submitUser.emit();
      }
    } else {
      this.disableNext.emit(true);
    }
  }

  populateUserSecurityQuestions() {
    this.setupForm.user.challengeList = [];
    this.setupForm.user.challengeList.push(this.challenge1);
    this.setupForm.user.challengeList.push(this.challenge2);
    this.setupForm.user.challengeList.push(this.challenge3);
    this.setupForm.user.securityQuestionsSet = true;
  }



}
