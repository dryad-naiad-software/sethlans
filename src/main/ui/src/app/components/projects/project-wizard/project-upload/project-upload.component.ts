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

import {Component, Input, OnInit} from '@angular/core';
import {ProjectWizardForm} from '../../../../models/forms/project_wizard_form.model';
import {Project} from '../../../../models/project.model';
import {ProjectType} from '../../../../enums/project_type.enum';

@Component({
  selector: 'app-project-upload',
  templateUrl: './project-upload.component.html',
  styleUrls: ['./project-upload.component.scss']
})
export class ProjectUploadComponent implements OnInit {
  @Input() projectWizard: ProjectWizardForm;
  uploading = false;

  constructor() {
  }


  ngOnInit() {

  }

  loadProjectDetails(event) {
    let response: any = JSON.parse(event.xhr.response);
    this.projectWizard.project = <Project>response;
    if (this.projectWizard.project.projectType == ProjectType.STILL_IMAGE) {
      this.projectWizard.project.endFrame = 1;
      this.projectWizard.project.stepFrame = 1;
    }
    if (this.projectWizard.project.selectedBlenderversion == null) {
      this.projectWizard.project.selectedBlenderversion = this.projectWizard.availableBlenderVersions[0];
    }
    this.projectWizard.project.useParts = true;
    this.projectWizard.project.partsPerFrame = 4;
    this.projectWizard.projectLoaded = true;

  }

  beforeSend(event: any) {
    this.uploading = true;
    event.xhr.setRequestHeader('X-XSRF-TOKEN', document.cookie.slice(document.cookie.indexOf('TOKEN=') + 'TOKEN='.length));
  }

}
