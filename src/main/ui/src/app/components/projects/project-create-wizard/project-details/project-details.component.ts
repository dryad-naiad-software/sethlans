/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {ProjectWizardForm} from '../../../../models/forms/project_wizard_form.model';
import {ProjectType} from '../../../../enums/project_type.enum';
import {RenderOutputFormat} from '../../../../enums/render_output_format.enum';

@Component({
  selector: 'app-project-details',
  templateUrl: './project-details.component.html',
  styleUrls: ['./project-details.component.scss']
})
export class ProjectDetailsComponent implements OnInit {
  @Input() projectWizard: ProjectWizardForm;
  projectTypes: any = ProjectType;
  formats: any = RenderOutputFormat;
  @ViewChild('projectDetailsForm') form: any;
  @Output() disableNext = new EventEmitter();


  constructor() {
  }

  ngOnInit() {
    if (this.projectWizard.project.projectName.length > 0) {
      this.validateForm();
    } else {
      this.disableNext.emit(true);
    }
  }

  validateForm() {
    if (this.form.valid) {
      this.disableNext.emit(false);
    } else {
      this.disableNext.emit(true);
    }
    console.log(this.form.valid);
  }

  setDefaultFormat() {
    this.projectWizard.project.outputFormat = RenderOutputFormat.PNG;
  }

  setDefaultAnimation() {
    this.projectWizard.project.endFrame = 50;
  }

}
