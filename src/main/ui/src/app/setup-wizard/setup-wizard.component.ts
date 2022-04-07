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
import {SethlansService} from "../services/sethlans.service";
import {SetupForm} from "../models/forms/setup-form.model";
import {SetupWizardProgress} from "../enums/setupwizardprogress.enum";
import {faCheck, faFlagCheckered, faGear, faSliders, faUserNinja} from "@fortawesome/free-solid-svg-icons";
import {faEye, faRectangleList} from "@fortawesome/free-regular-svg-icons";
import {Mode} from "../enums/mode.enum";
import {UserChallenge} from "../models/user/user-challenge.model";
import {Role} from "../enums/role.enum";
import {LogLevel} from "../enums/loglevel.enum";
import {NodeType} from "../enums/nodetype.enum";
import {GPU} from "../models/hardware/gpu.model";

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
  faSliders = faSliders;
  faCheck = faCheck;
  faRectangleList = faRectangleList;
  formSent: boolean = false;
  Mode = Mode;
  LogLevel = LogLevel;
  NodeType = NodeType;
  usernameRegEx = new RegExp('^[a-zA-Z0-9]{4,}$')
  emailRegEx = new RegExp('^[^\\s@]+@[^\\s@]+\\.[^\\s@]{1,}$')
  passwordRegEx = new RegExp('^.{8,35}$')
  usernameError = false;
  emailError = false;
  challenge1 = ''
  challenge2 = ''
  challenge3 = ''
  response1 = ''
  response2 = ''
  response3 = ''
  passwordError = false;
  passwordMatch = true;
  responseError1 = true;
  responseError2 = true;
  responseError3 = true;
  confirmPass = '';
  showPass = false;
  showMailSettings = false;
  nextEnabled = true;
  modeSettingsEnabled = false;
  settingsEnabled = false;
  summaryEnabled = false;

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
      if (this.setupForm.availableTypes.length > 1) {
        this.setupForm.nodeSettings.nodeType = NodeType.CPU_GPU;
        this.setupForm.nodeSettings.tileSizeGPU = 256;
        this.setupForm.nodeSettings.selectedGPUs = new Array<GPU>()
      } else {
        this.setupForm.nodeSettings.nodeType = NodeType.CPU;
      }
      this.setupForm.nodeSettings.cores = this.setupForm.systemInfo.cpu.cores - 1;
      this.setupForm.nodeSettings.tileSizeCPU = 32;
      this.setupForm.user.challengeList = new Array<UserChallenge>();
      this.setupForm.user.roles = new Array<Role>()
      this.setupForm.user.roles.push(Role.SUPER_ADMINISTRATOR)
      this.setupForm.user.active = true;
      this.setupForm.user.username = ''
      this.setupForm.user.password = ''
      this.setupForm.user.email = ''
      let challenge1: UserChallenge = {
        challenge: this.setupForm.challengeQuestions[0],
        response: '',
        responseUpdated: false
      };
      let challenge2: UserChallenge = {
        challenge: this.setupForm.challengeQuestions[1],
        response: '',
        responseUpdated: false
      };
      let challenge3: UserChallenge = {
        challenge: this.setupForm.challengeQuestions[3],
        response: '',
        responseUpdated: false
      };
      this.challenge1 = this.setupForm.challengeQuestions[0];
      this.challenge2 = this.setupForm.challengeQuestions[1];
      this.challenge3 = this.setupForm.challengeQuestions[2];

      this.setupForm.user.challengeList.push(challenge1);
      this.setupForm.user.challengeList.push(challenge2);
      this.setupForm.user.challengeList.push(challenge3);
    })
  }

  checkUser() {
    if (this.setupForm?.user.username == '') {
      return false;
    } else if (this.setupForm?.user.password == '') {
      return false;
    } else if (this.confirmPass != this.setupForm?.user.password) {
      return false;
    } else if (this.usernameError || this.responseError1 || this.responseError2 || this.responseError3
      || this.passwordError) {
      return false;
    } else if (this.setupForm.user.email == '' && this.setupForm.mode != Mode.NODE) {
      return false;
    } else if (this.emailError && this.setupForm?.mode != Mode.NODE) {
      return false;
    } else {
      return true;
    }
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
    // @ts-ignore
    challenge?.responseUpdated = true;
    if (id == 0) {
      this.response1 = response;
    }
    if (id == 1) {
      this.response2 = response;
    }
    if (id == 3) {
      this.response3 = response;
    }
    this.validateResponse(id, response);
  }

  validateUsername() {
    this.usernameError = !this.setupForm?.user.username.match(this.usernameRegEx);
    this.nextEnabled = this.checkUser()
  }

  validateEmail() {
    this.emailError = !this.setupForm?.user.email.match(this.emailRegEx);
    this.nextEnabled = this.checkUser()

  }

  validatePassword() {
    this.passwordError = !this.setupForm?.user.password.match(this.passwordRegEx);
    this.nextEnabled = this.checkUser()

  }

  checkPasswordConfirm() {
    this.passwordMatch = this.setupForm?.user.password === this.confirmPass;
    this.nextEnabled = this.checkUser()
  }

  validateResponse(id: number, response: string) {
    if (id == 0) {
      this.responseError1 = !(response.length > 4);
    }
    if (id == 1) {
      this.responseError2 = !(response.length > 4);
    }
    if (id == 2) {
      this.responseError3 = !(response.length > 4);
    }
    this.nextEnabled = this.checkUser();

  }

  checkGPUs() {

    if (this.setupForm?.mode != Mode.SERVER) {
      if (this.setupForm?.nodeSettings.nodeType != NodeType.CPU
        && this.setupForm?.nodeSettings.selectedGPUs.length == 0) {
        return false;
      }
    }
    return true;
  }

  goToMode() {
    this.nextEnabled = true;
    this.setupWizardProgress = SetupWizardProgress.MODE;
  }

  goToAdmin() {
    this.nextEnabled = this.checkUser();
    this.setupWizardProgress = SetupWizardProgress.ADMIN_SETUP;
  }

  goToModeSetup() {
    this.nextEnabled = this.checkGPUs();
    this.setupWizardProgress = SetupWizardProgress.MODE_SETUP;

  }

  goToSettings() {
    this.setupWizardProgress = SetupWizardProgress.SETTINGS;
    if (this.setupForm?.mode == Mode.NODE) {
      this.showMailSettings = false;
    }
  }

  goToSummary() {
    this.setupWizardProgress = SetupWizardProgress.SUMMARY;
  }


  previous() {
    switch (this.setupWizardProgress) {
      case SetupWizardProgress.ADMIN_SETUP:
        this.goToMode();
        break;
      case SetupWizardProgress.MODE_SETUP:
        this.goToAdmin();
        break;
      case SetupWizardProgress.SETTINGS:
        this.goToModeSetup();
        break;
      case SetupWizardProgress.SUMMARY:
        this.goToSettings();
        break;
    }

  }

  finish() {
    this.setupWizardProgress = SetupWizardProgress.FINISHED;

  }

  next() {
    switch (this.setupWizardProgress) {
      case SetupWizardProgress.MODE:
        this.goToAdmin();
        break;
      case SetupWizardProgress.ADMIN_SETUP:
        this.modeSettingsEnabled = true;
        this.goToModeSetup();
        break;
      case SetupWizardProgress.MODE_SETUP:
        this.settingsEnabled = true;
        this.goToSettings();
        break;
      case SetupWizardProgress.SETTINGS:
        this.summaryEnabled = true;
        this.goToSummary();
        break;
    }

  }

}
