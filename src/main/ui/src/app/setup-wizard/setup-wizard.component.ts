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

  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
  }

  loadSetupForm() {
    this.sethlansService.getSetup().subscribe((obj: any) => {
      this.setupForm = new SetupForm(obj)
      this.setupForm.mode = Mode.DUAL;
      this.setupWizardProgress = SetupWizardProgress.MODE;
    })
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
