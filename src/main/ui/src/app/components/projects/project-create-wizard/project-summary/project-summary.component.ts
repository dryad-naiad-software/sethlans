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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ProjectWizardForm} from '../../../../models/forms/project_wizard_form.model';
import {ProjectType} from '../../../../enums/project_type.enum';
import {BlenderEngine} from '../../../../enums/blender_engine.enum';
import {ProjectWizardProgress} from '../../../../enums/project_wizard_progress';
import {ImageOutputFormat} from '../../../../enums/image_output_format.enum';
import {AnimationType} from '../../../../enums/animation_type.enum';
import {VideoCodec} from '../../../../enums/video_codec.enum';
import {VideoQuality} from '../../../../enums/video_quality.enum';

@Component({
  selector: 'app-project-summary',
  templateUrl: './project-summary.component.html',
  styleUrls: ['./project-summary.component.scss']
})
export class ProjectSummaryComponent implements OnInit {
  @Output() disableNext = new EventEmitter();
  @Input() projectWizard: ProjectWizardForm;
  @Input() isEdit: boolean;
  projectTypes: any = ProjectType;
  outputFormat: any = ImageOutputFormat;
  animationTypes: any = AnimationType;
  engines: any = BlenderEngine;
  codecs: any = VideoCodec;
  wizardProgress: any = ProjectWizardProgress;
  videoQuality: any = VideoQuality;


  constructor() {
  }

  ngOnInit() {
    console.log(this.projectWizard.project);
  }

}
