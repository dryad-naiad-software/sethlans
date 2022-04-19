/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {ProjectType} from "../../enums/projectype.enum";
import {ProjectSettings} from "../settings/projectsettings.model";

/**
 * File created by Mario Estrella on 4/18/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class ProjectForm {
  projectName: string;
  projectType: ProjectType;
  projectSettings: ProjectSettings;
  projectID: string;
  originalFile: string;
  projectFileLocation: string;
  installedBlenderVersions: Array<string>

  constructor() {
    this.projectName = '';
    this.projectType = ProjectType.STILL_IMAGE;
    this.projectSettings = new ProjectSettings();
    this.projectID = '';
    this.originalFile = '';
    this.projectFileLocation = '';
    this.installedBlenderVersions = new Array<string>();
  }

  setProjectForm(obj: any) {
    this.projectName = obj.projectName;
    this.projectType = obj.projectType;
    this.projectSettings = obj.projectSettings;
    this.projectID = obj.projectID;
    this.originalFile = obj.originalFile;
    this.projectFileLocation = obj.projectFileLocation;
    this.installedBlenderVersions = obj.installedBlenderVersions;
  }
}
