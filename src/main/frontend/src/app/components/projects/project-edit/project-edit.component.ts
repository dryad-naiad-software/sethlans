/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {RenderOutputFormat} from "../../../enums/render_output_format.enum";
import {Project} from "../../../models/project.model";
import {BlenderEngine} from "../../../enums/blender_engine.enum";
import {ProjectType} from "../../../enums/project_type.enum";

@Component({
  selector: 'app-project-edit',
  templateUrl: './project-edit.component.html',
  styleUrls: ['./project-edit.component.scss']
})
export class ProjectEditComponent implements OnInit {
  projectDetails: Project;
  projectTypes = ProjectType;
  availableBlenderVersions: any[];
  projectLoaded: boolean = false;
  useParts: boolean = true;
  formats = RenderOutputFormat;
  computeMethods = ComputeMethod;
  engines = BlenderEngine;
  status: number = 0;

  constructor(private http: HttpClient, private router: Router) {
  }

  ngOnInit() {
  }

  loadProjectDetails(event) {
    let response: any = JSON.parse(event.xhr.response);
    this.projectDetails = <Project>response;
    if (this.projectDetails.projectType == this.projectTypes.STILL_IMAGE) {
      this.projectDetails.endFrame = 1;
      this.projectDetails.stepFrame = 1;
    }
    if (this.projectDetails.selectedBlenderversion == null) {
      this.projectDetails.selectedBlenderversion = this.availableBlenderVersions[0];
    }
    console.log(this.projectDetails);
    this.projectLoaded = true;
  }

  getAvailableBlenderVersions() {
    this.http.get('/api/info/installed_blender_versions')
      .subscribe(
        (blenderVersions: any[]) => {
          this.availableBlenderVersions = blenderVersions;
        });
  }

  projectConfigNext() {
    this.status = this.status + 1;
  }

  projectConfigPrevious() {
    this.status = this.status - 1;
  }

  returnToProjects(): void {
    this.router.navigateByUrl("/projects").then(() => location.reload());
    ;
  }

}
