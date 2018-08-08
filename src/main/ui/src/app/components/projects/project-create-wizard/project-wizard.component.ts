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
import {ProjectWizardForm} from '../../../models/forms/project_wizard_form.model';
import {ProjectWizardProgress} from '../../../enums/project_wizard_progress';
import {HttpClient, HttpHeaders} from '@angular/common/http';

@Component({
  selector: 'app-project-wizard',
  templateUrl: './project-wizard.component.html',
  styleUrls: ['./project-wizard.component.scss']
})
export class ProjectWizardComponent implements OnInit {
  projectWizard: ProjectWizardForm;
  wizardProgress: any = ProjectWizardProgress;
  nextDisabled: boolean;

  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.projectWizard = new ProjectWizardForm();
  }

  ngOnInit() {
    this.getAvailableBlenderVersions();
    this.nextDisabled = true;
  }

  disableNext(value: boolean) {
    this.nextDisabled = value;
  }

  returnToProjects(): void {
    window.location.href = '/projects/';
  }

  submit() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    if (this.projectWizard.project.useParts == false) {
      this.projectWizard.project.partsPerFrame = 1;
    }
    this.http.post('/api/project_form/submit_project', JSON.stringify(this.projectWizard.project), httpOptions).subscribe(() => {
      this.projectWizard.currentProgress = ProjectWizardProgress.FINISHED;
    });
  }

  next() {
    switch (this.projectWizard.currentProgress) {
      case ProjectWizardProgress.PROJECT_DETAILS:
        this.projectWizard.detailsValid = true;
        this.projectWizard.currentProgress = ProjectWizardProgress.RENDER_SETTINGS;
        break;
      case ProjectWizardProgress.RENDER_SETTINGS:
        this.projectWizard.formComplete = true;
        this.projectWizard.currentProgress = ProjectWizardProgress.SUMMARY;
        break;
    }
  }

  previous() {
    switch (this.projectWizard.currentProgress) {
      case ProjectWizardProgress.RENDER_SETTINGS:
        this.projectWizard.currentProgress = ProjectWizardProgress.PROJECT_DETAILS;
        break;
      case ProjectWizardProgress.SUMMARY:
        this.projectWizard.currentProgress = ProjectWizardProgress.RENDER_SETTINGS;
        break;
    }
  }

  getAvailableBlenderVersions() {
    this.http.get('/api/info/installed_blender_versions')
      .subscribe(
        (blenderVersions: string[]) => {
          this.projectWizard.availableBlenderVersions = blenderVersions;
        });
  }

}
