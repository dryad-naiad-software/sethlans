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
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
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
  id: number;
  frameRates: string[] = ["23.98", "24", "25", "29.97", "30", "50", "59.94", "60"];


  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.getAvailableBlenderVersions();
    this.route.params.subscribe(params => {
      this.id = +params['id'];
      this.loadProjectDetails();
    })
  }

  setParts() {
    if (this.projectDetails.useParts == true) {
      this.projectDetails.partsPerFrame = 4;
    } else {
      this.projectDetails.partsPerFrame = 1;
    }
  }

  loadProjectDetails() {
    this.http.get('/api/project_ui/project_details/' + this.id + '/').subscribe((projectDetails: Project) => {
      this.projectDetails = projectDetails;
      this.projectLoaded = true;
      console.log(projectDetails);
    });
  }

  editProject() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    if (this.projectDetails.useParts == false) {
      this.projectDetails.partsPerFrame = 1;
    }
    this.http.post('/api/project_form/edit_project/' + this.id + '/', JSON.stringify(this.projectDetails), httpOptions).subscribe((success: boolean) => {
      this.router.navigateByUrl("/projects").then(() => {
        location.reload();
      });
    })
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
  }

  setDefaultFormat() {
    this.projectDetails.outputFormat = "PNG";
  }

  setDefaultFrameRate() {
    this.projectDetails.frameRate = "23.98";
  }


}
