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

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.scss']
})
export class SetupWizardComponent implements OnInit {
  progress: SetupProgress;
  setupProgress: any = SetupProgress;
  setupForm: SetupForm;

  constructor() {
    this.progress = SetupProgress.MODE_SELECT;
    this.setupForm = new SetupForm();
    document.body.style.background = 'rgba(0, 0, 0, .6)';
  }

  ngOnInit() {
  }

  next() {
    switch (this.progress) {
      case SetupProgress.MODE_SELECT: {
        this.progress = SetupProgress.REGISTER_USER;
        break;
      }
    }
  }

  previous() {
    switch (this.progress) {
      case SetupProgress.REGISTER_USER: {
        this.progress = SetupProgress.MODE_SELECT;
        break;
      }
    }
  }

}
