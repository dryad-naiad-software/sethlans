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
import {GetStartedProgress} from '../../../enums/get_started_progress.enum';
import {GetStartedWizardForm} from '../../../models/forms/get_started_wizard_form.model';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-get-started',
  templateUrl: './get-started-wizard.component.html',
  styleUrls: ['./get-started-wizard.component.scss']
})
export class GetStartedWizardComponent implements OnInit {
  getStartedWizardForm: GetStartedWizardForm;
  wizardProgress: any = GetStartedProgress;
  onStartCheckBox: boolean;
  disablePrevious: boolean;
  nextDisabled: boolean;



  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.disablePrevious = true;
    this.nextDisabled = false;
  }

  ngOnInit() {
    this.getStartedWizardForm = new GetStartedWizardForm();
    this.http.get('/api/info/get_started').subscribe((response: boolean) => {
      this.onStartCheckBox = response;
    });
  }

  setOnStart() {
    console.log(this.onStartCheckBox);
    this.http.get('/api/management/change_get_started_wizard/?value=' + this.onStartCheckBox).subscribe();

  }

  next() {
    switch (this.getStartedWizardForm.currentProgress) {
      case GetStartedProgress.START:
        this.getStartedWizardForm.currentProgress = GetStartedProgress.NODE_AUTH;
        this.disablePrevious = false;
        this.nextDisabled = true;
        break;
    }

  }

  previous() {
    switch (this.getStartedWizardForm.currentProgress) {
      case GetStartedProgress.NODE_AUTH:
        this.getStartedWizardForm.currentProgress = GetStartedProgress.START;
        this.nextDisabled = false;
        this.disablePrevious = true;
        break;
    }

  }

}
