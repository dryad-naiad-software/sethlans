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


import {Project} from '../project.model';
import {ProjectWizardProgress} from '../../enums/project_wizard_progress';

export class ProjectWizardForm {
  project: Project;
  currentProgress: ProjectWizardProgress;
  detailsValid: boolean;
  formComplete: boolean;
  finished: boolean;
  projectLoaded: boolean;
  videoEnabled: boolean;
  availableBlenderVersions: string[];


  constructor() {
    this.project = new Project();
    this.currentProgress = ProjectWizardProgress.UPLOAD;
    this.finished = false;
    this.projectLoaded = false;
    this.detailsValid = false;
    this.videoEnabled = false;
    this.formComplete = false;
  }
}
