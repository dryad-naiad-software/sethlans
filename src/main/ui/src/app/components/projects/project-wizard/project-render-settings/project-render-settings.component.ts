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
import {ProjectWizardForm} from '../../../../models/forms/project_wizard_form.model';
import {ComputeMethod} from '../../../../enums/compute.method.enum';
import {BlenderEngine} from '../../../../enums/blender_engine.enum';

@Component({
  selector: 'app-project-render-settings',
  templateUrl: './project-render-settings.component.html',
  styleUrls: ['./project-render-settings.component.scss']
})
export class ProjectRenderSettingsComponent implements OnInit {
  @Output() disableNext = new EventEmitter();
  @Input() projectWizard: ProjectWizardForm;
  computeMethods: any = ComputeMethod;
  engines: any = BlenderEngine;

  constructor() {
  }

  ngOnInit() {
  }

  setParts() {
    if (this.projectWizard.project.useParts == true) {
      this.projectWizard.project.partsPerFrame = 4;
    } else {
      this.projectWizard.project.partsPerFrame = 1;
    }
  }

}
