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
import {ProjectStatus} from "./projectstatus.model";

/**
 * File created by Mario Estrella on 4/13/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class ProjectView {
  projectID: string;
  userID: string;
  projectType: ProjectType;
  projectSettings: ProjectSettings;
  projectStatus: ProjectStatus;
  projectName: string;
  projectRootDir: string;

  constructor() {
    this.projectID = '';
    this.userID = '';
    this.projectType = ProjectType.STILL_IMAGE;
    this.projectSettings = new ProjectSettings();
    this.projectStatus = new ProjectStatus();
    this.projectName = '';
    this.projectRootDir = '';
  }
}
