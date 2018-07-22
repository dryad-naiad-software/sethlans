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
import {SetupProgress} from '../../../enums/setupProgress.enum';
import {SetupForm} from '../../../models/setupForm.model';
import {Mode} from '../../../enums/mode.enum';

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.scss']
})
export class SetupWizardComponent implements OnInit {
  progress: SetupProgress;
  setupProgress: any = SetupProgress;
  modes: any = Mode;
  setupForm: SetupForm;
  nextDisabled: any;

  constructor() {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
  }

  ngOnInit() {
    this.progress = SetupProgress.MODE_SELECT;
    this.setupForm = new SetupForm();
  }

  disableNext(value: boolean) {
    this.nextDisabled = value;
  }

  submitUser() {
    this.next();
  }

  next() {
    switch (this.progress) {
      case SetupProgress.MODE_SELECT: {
        this.progress = SetupProgress.REGISTER_USER;
        break;
      }
      case SetupProgress.REGISTER_USER: {
        this.progress = SetupProgress.MODE_CONFIG;
        this.setupForm.user.active = true;
        break;
      }
      case SetupProgress.MODE_CONFIG: {
        this.setupForm.isModeDone = true;
        this.progress = SetupProgress.SETTINGS;
        break;
      }
      case SetupProgress.SETTINGS: {
        this.progress = SetupProgress.SUMMARY;
        break;
      }
    }
  }

  previous() {
    switch (this.progress) {
      case SetupProgress.REGISTER_USER: {
        this.progress = SetupProgress.MODE_SELECT;
        this.nextDisabled = false;
        break;
      }
      case SetupProgress.MODE_CONFIG: {
        this.progress = SetupProgress.REGISTER_USER;
        break;
      }
      case SetupProgress.SETTINGS: {
        this.progress = SetupProgress.MODE_CONFIG;
        break;
      }
      case SetupProgress.SUMMARY: {
        this.progress = SetupProgress.SETTINGS;
        break;
      }
    }
  }

}
