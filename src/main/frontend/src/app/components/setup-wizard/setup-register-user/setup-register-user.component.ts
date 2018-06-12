/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from '../../../services/setupformdata.service';
import {User} from '../../../models/user.model';
import {Mode} from '../../../enums/mode.enum';

@Component({
  selector: 'app-setup-register-user',
  templateUrl: './setup-register-user.component.html',
  styleUrls: ['./setup-register-user.component.scss']
})
export class SetupRegisterUserComponent implements OnInit {
  @Input() setupFormData;
  user: User;

  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.user = this.setupFormData.getUser();
  }

  save() {
    this.setupFormDataService.setUser(this.user);
    this.nextStep();

  }

  previousStep() {
    let currentProgress = this.setupFormData.getProgress();
    this.setupFormData.setProgress(currentProgress - 1);
    // this.user.setPassword('');
    // this.user.setPasswordConfirm('');
  }

  nextStep() {
    let currentMode = this.setupFormData.getMode();
    if (currentMode === Mode.SERVER) {
      this.setupFormData.setProgress(2);
    }
    if (currentMode === Mode.NODE) {
      this.setupFormData.setProgress(3);
    }

    if (currentMode === Mode.DUAL) {
      this.setupFormData.setProgress(4);
    }

  }

  userSubmit(event, form: any) {
    if (event.key === 'Enter' && form.valid) {
      this.save();
    }

  }
}
