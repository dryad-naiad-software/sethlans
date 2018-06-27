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
import {Project} from '../../../models/project.model';
import {ProjectType} from '../../../enums/project_type.enum';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {ComputeMethod} from '../../../enums/compute.method.enum';
import {RenderOutputFormat} from '../../../enums/render_output_format.enum';
import {BlenderEngine} from '../../../enums/blender_engine.enum';

@Component({
  selector: 'app-project-add',
  templateUrl: './project-add.component.html',
  styleUrls: ['./project-add.component.scss']
})
export class ProjectAddComponent implements OnInit {
  projectDetails: Project;
  projectTypes = ProjectType;
  availableBlenderVersions: any[];
  projectLoaded: boolean = false;
  formats = RenderOutputFormat;
  computeMethods = ComputeMethod;
  engines = BlenderEngine;
  status: number = 0;
  uploading: boolean = false;
  frameRates: string[] = ["23.98", "24", "25", "29.97", "30", "50", "59.94", "60"];


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.getAvailableBlenderVersions();
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
    this.projectDetails.useParts = true;
    this.projectDetails.partsPerFrame = 4;
    this.projectLoaded = true;
  }

  getAvailableBlenderVersions() {
    this.http.get('/api/info/installed_blender_versions')
      .subscribe(
        (blenderVersions: any[]) => {
          this.availableBlenderVersions = blenderVersions;
        });
  }

  beforeSend(event: any) {
    this.uploading = true;
    event.xhr.setRequestHeader('X-XSRF-TOKEN', document.cookie.slice(document.cookie.indexOf("TOKEN=") + "TOKEN=".length));
  }

  submitProject() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    if (this.projectDetails.useParts == false) {
      this.projectDetails.partsPerFrame = 1;
    }
    this.http.post('/api/project_form/submit_project', JSON.stringify(this.projectDetails), httpOptions).subscribe(() => {
      window.location.href = '/projects/';
    })
  }

  projectConfigNext() {
    this.status = this.status + 1;
  }

  projectConfigPrevious() {
    this.status = this.status - 1;
  }

  returnToProjects(): void {
    window.location.href = '/projects/';
  }

  setParts() {
    if (this.projectDetails.useParts == true) {
      this.projectDetails.partsPerFrame = 4;
    } else {
      this.projectDetails.partsPerFrame = 1;
    }
  }

  setDefaultFormat() {
    this.projectDetails.outputFormat = "PNG";
  }

  setDefaultFrameRate() {
    this.projectDetails.frameRate = "23.98";
    this.projectDetails.endFrame = 50;
  }

}
