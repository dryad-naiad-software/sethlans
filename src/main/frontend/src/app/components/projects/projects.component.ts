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
import {AfterViewInit, Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {Project} from "../../models/project.model";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {RenderOutputFormat} from "../../enums/render_output_format.enum";
import {ProjectType} from "../../enums/project_type.enum";
import {ComputeMethod} from "../../enums/compute.method.enum";
import {BlenderEngine} from "../../enums/blender_engine.enum";
import {ProjectListService} from "../../services/project_list.service";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit, AfterViewInit {

  placeholder: any = "assets/images/placeholder.svg";
  nodesReady: boolean = false;
  projectSize: number;
  projectDetails: Project;
  projects: Project[];
  availableBlenderVersions: any[];
  formats = RenderOutputFormat;
  projectTypes = ProjectType;
  computeMethods = ComputeMethod;
  engines = BlenderEngine;
  useParts: boolean = true;
  dtOptions: DataTables.Settings = {};


  constructor(private http: HttpClient, private modalService: NgbModal, private projectService: ProjectListService) {
  }

  ngAfterViewInit(): void {

  }

  ngOnInit() {
    this.getNodeStatus();
    this.getAvailableBlenderVersions();
    this.getProjectListSize();
    this.dtOptions = {
      searching: false,
      ordering: false
    };
    this.projectService.getProjectList().subscribe(value => {
      this.projects = value;
    });

    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getNodeStatus();
      this.getProjectListSize();
    });
  }

  getAvailableBlenderVersions() {
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions: any[]) => {
          this.availableBlenderVersions = blenderVersions;
        });
  }

  getProjectListSize() {
    this.http.get<number>("/api/project_ui/num_of_projects").subscribe((projectSize: number) => {
      this.projectSize = projectSize;
    });
  }


  getNodeStatus() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });
  }

  submitProject() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    if (this.useParts == false) {
      this.projectDetails.partsPerFrame = 1;
    }
    this.http.post('/api/project_form/submit_project', JSON.stringify(this.projectDetails), httpOptions).subscribe()

  }

  loadProjectDetails(content, event) {
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
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }

  beforeSend(event: any) {
    event.xhr.setRequestHeader('X-XSRF-TOKEN', document.cookie.slice(document.cookie.indexOf("TOKEN=") + "TOKEN=".length));
  }


  openModal(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }


}
