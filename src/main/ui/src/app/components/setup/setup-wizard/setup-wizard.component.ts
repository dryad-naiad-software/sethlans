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
import {SetupWizardProgress} from '../../../enums/setup_wizard_progress.enum';
import {SetupWizardForm} from '../../../models/forms/setup_wizard_form.model';
import {Mode} from '../../../enums/mode.enum';
import {HttpClient, HttpHeaders} from '@angular/common/http';

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.scss']
})
export class SetupWizardComponent implements OnInit {
  progress: SetupWizardProgress;
  setupProgress: any = SetupWizardProgress;
  modes: any = Mode;
  setupForm: SetupWizardForm;
  nextDisabled: any;
  modeSelected: boolean;
  settingsComplete: boolean;
  finishedActive: boolean;

  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
  }

  ngOnInit() {
    this.progress = SetupWizardProgress.MODE_SELECT;
    this.setupForm = new SetupWizardForm();
    this.modeSelected = false;
    this.settingsComplete = false;
    this.finishedActive = false;
  }

  disableNext(value: boolean) {
    this.nextDisabled = value;
  }

  next() {
    switch (this.progress) {
      case SetupWizardProgress.MODE_SELECT: {
        this.progress = SetupWizardProgress.REGISTER_USER;
        this.modeSelected = true;
        break;
      }
      case SetupWizardProgress.REGISTER_USER: {
        this.progress = SetupWizardProgress.MODE_CONFIG;
        this.setupForm.user.active = true;
        break;
      }
      case SetupWizardProgress.MODE_CONFIG: {
        this.setupForm.isModeDone = true;
        this.progress = SetupWizardProgress.SETTINGS;
        break;
      }
      case SetupWizardProgress.SETTINGS: {
        if (this.setupForm.showMailSettings && !this.setupForm.configureMail) {
          this.progress = SetupWizardProgress.SUMMARY;
          this.settingsComplete = true;

        }
        if (this.setupForm.mailSettingsComplete && this.setupForm.configureMail) {
          this.progress = SetupWizardProgress.SUMMARY;
          this.settingsComplete = true;
          break;
        } else {
          this.setupForm.showMailSettings = true;
          break;
        }



      }
    }
  }

  previous() {
    switch (this.progress) {
      case SetupWizardProgress.REGISTER_USER: {
        this.progress = SetupWizardProgress.MODE_SELECT;
        this.nextDisabled = false;
        break;
      }
      case SetupWizardProgress.MODE_CONFIG: {
        this.progress = SetupWizardProgress.REGISTER_USER;
        break;
      }
      case SetupWizardProgress.SETTINGS: {
        if (this.setupForm.showMailSettings) {
          this.setupForm.showMailSettings = false;
          this.setupForm.mailSettingsComplete = false;
          this.nextDisabled = false;
          break;
        } else {
          this.progress = SetupWizardProgress.MODE_CONFIG;
          break;
        }

      }
      case SetupWizardProgress.SUMMARY: {
        this.progress = SetupWizardProgress.SETTINGS;
        this.setupForm.showMailSettings = true;
        break;
      }
    }
  }

  finish() {
    this.setupForm.complete = true;
    if (!this.setupForm.mailSettings.startTLSEnabled) {
      this.setupForm.mailSettings.startTLSRequired = false;
    }
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/setup/submit', JSON.stringify(this.setupForm), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        this.progress = SetupWizardProgress.FINISHED;
        this.finishedActive = true;
      }
    });
  }

}
