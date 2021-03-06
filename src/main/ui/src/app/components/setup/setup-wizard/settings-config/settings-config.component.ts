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
import {HttpClient} from '@angular/common/http';
import {Mode} from '../../../../enums/mode.enum';

@Component({
  selector: 'app-settings-config',
  templateUrl: './settings-config.component.html',
  styleUrls: ['./settings-config.component.scss']
})
export class SettingsConfigComponent implements OnInit {
  @Input() setupForm: SetupWizardForm;
  @Output() disableNext = new EventEmitter();

  constructor(private http: HttpClient) {

  }

  ngOnInit() {
    if (this.setupForm.mode === Mode.NODE) {
      this.setupForm.configureMail = false;
    }
    if (this.setupForm.rootDirectory == null) {
      this.http.get('/api/info/root_directory',)
        .subscribe((rootDirectory) => {
          this.setupForm.rootDirectory = rootDirectory['root_dir'];
        });
    }
    if (this.setupForm.ipAddress == null) {
      this.http.get('/api/info/sethlans_ip')
        .subscribe((sethlansIP) => {
          this.setupForm.ipAddress = sethlansIP['ip'];
        });
    }
    if (this.setupForm.port == null) {
      this.http.get('/api/info/sethlans_port')
        .subscribe((sethlansPort) => {
          this.setupForm.port = sethlansPort['port'];
        });
    }
    if (this.setupForm.appURL == null) {
      this.http.get('/api/info/app_url').subscribe((appURL) => {
        this.setupForm.appURL = appURL['app_url'];
      });
    }

  }

  validateAndSubmit(event, settingsForm) {
    if (settingsForm.valid) {
      this.disableNext.emit(false);
      if (this.setupForm.showMailSettings) {
        this.setupForm.mailSettingsComplete = true;
      }
    } else {
      this.disableNext.emit(true);
      if (this.setupForm.showMailSettings) {
        this.setupForm.mailSettingsComplete = false;
      }
    }
  }

  skipMail() {
    if (!this.setupForm.configureMail) {
      this.setupForm.mailSettingsComplete = true;
      this.disableNext.emit(false);
    } else {
      this.setupForm.mailSettingsComplete = false;
      this.disableNext.emit(true);
    }
  }

  smtpAuthValid() {
    if (this.setupForm.mailSettings.smtpAuth) {
      if (this.setupForm.mailSettings.username.length != 0 || this.setupForm.mailSettings.password.length != 0) {
        console.log('true');
        this.disableNext.emit(false);
      } else {
        this.disableNext.emit(true);
      }
    }
  }

}
