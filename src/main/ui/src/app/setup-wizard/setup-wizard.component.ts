/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../sethlans.service";
import {SetupForm} from "../models/forms/setup-form.model";
import {SetupWizardProgress} from "../enums/setupwizardprogress.enum";
import {faFlagCheckered, faGear, faUserNinja} from "@fortawesome/free-solid-svg-icons";
import {faEye} from "@fortawesome/free-regular-svg-icons";
import {Mode} from "../enums/mode.enum";
import {UserChallenge} from "../models/user/user-challenge.model";
import {Role} from "../enums/role.enum";

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.css']
})

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class SetupWizardComponent implements OnInit {
  setupForm: SetupForm | undefined;
  setupWizardProgress: SetupWizardProgress = SetupWizardProgress.BEGIN;
  SetupWizardProgress = SetupWizardProgress;
  faFlagCheckered = faFlagCheckered;
  faUserNinja = faUserNinja;
  faGear = faGear;
  faEye = faEye;
  formSent: boolean = false;
  Mode = Mode;
  usernameRegEx = new RegExp('^[a-zA-Z0-9]{4,}$')
  emailRegEx = new RegExp('^[^\\s@]+@[^\\s@]+\\.[^\\s@]{1,}$')
  passwordRegEx = new RegExp('^.{8,35}$')
  usernameError = false;
  emailError = false;
  passwordError = false;
  passwordMatch = true;
  confirmPass = '';
  showPass = false;

  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
  }

  loadSetupForm() {
    this.sethlansService.getSetup().subscribe((obj: any) => {
      this.setupForm = new SetupForm(obj)
      this.setupForm.mode = Mode.DUAL;
      this.setupWizardProgress = SetupWizardProgress.MODE;
      this.setupForm.serverSettings.blenderVersion = this.setupForm.blenderVersions[0];
      this.setupForm.user.challengeList = new Array<UserChallenge>();
      this.setupForm.user.roles = new Array<Role>()
      this.setupForm.user.roles.push(Role.SUPER_ADMINISTRATOR)
      this.setupForm.user.active = true;
      this.setupForm.user.username = ''
      this.setupForm.user.password = ''
      this.setupForm.user.email = ''
      let challenge1: UserChallenge = {
        challenge: this.setupForm.challengeQuestions[0],
        response: 'answer',
        responseUpdated: false
      };
      let challenge2: UserChallenge = {
        challenge: this.setupForm.challengeQuestions[0],
        response: 'answer',
        responseUpdated: false
      };
      let challenge3: UserChallenge = {
        challenge: this.setupForm.challengeQuestions[0],
        response: 'answer',
        responseUpdated: false
      };
      this.setupForm.user.challengeList.push(challenge1);
      this.setupForm.user.challengeList.push(challenge2);
      this.setupForm.user.challengeList.push(challenge3);
    })
  }

  updateChallenge(id: number, question: string) {
    let challenge: UserChallenge | undefined = this.setupForm?.user.challengeList[id];
    // @ts-ignore
    challenge.challenge = question;
  }

  updateResponse(id: number, response: string) {
    let challenge: UserChallenge | undefined = this.setupForm?.user.challengeList[id];
    // @ts-ignore
    challenge?.response = response;


  }

  validateUsername() {
    this.usernameError = !this.setupForm?.user.username.match(this.usernameRegEx);
  }

  validateEmail() {
    this.emailError = !this.setupForm?.user.email.match(this.emailRegEx);
  }

  validatePassword() {
    this.passwordError = !this.setupForm?.user.password.match(this.passwordRegEx);
  }

  checkPasswordConfirm() {
    this.passwordMatch = this.setupForm?.user.password === this.confirmPass;
  }

  goToMode() {
    this.setupWizardProgress = SetupWizardProgress.MODE;
  }

  goToAdmin() {
    this.setupWizardProgress = SetupWizardProgress.ADMIN_SETUP;
  }

  goToModeSetup() {
    this.setupWizardProgress = SetupWizardProgress.MODE_SETUP;

  }

  previous() {
    switch (this.setupWizardProgress) {
      case SetupWizardProgress.ADMIN_SETUP:
        this.goToMode();
        break;
      case SetupWizardProgress.MODE_SETUP:
        this.goToAdmin();
        break;
    }

  }

  next() {
    switch (this.setupWizardProgress) {
      case SetupWizardProgress.MODE:
        this.goToAdmin();
        break;
      case SetupWizardProgress.ADMIN_SETUP:
        this.goToModeSetup();
    }

  }

}
