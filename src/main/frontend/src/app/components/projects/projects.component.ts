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
import {Subject} from "rxjs/Subject";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {Project} from "../../models/project.model";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {RenderOutputFormat} from "../../enums/render_output_format.enum";
import {ProjectType} from "../../enums/project_type.enum";
import {ComputeMethod} from "../../enums/compute.method.enum";
import {BlenderEngine} from "../../enums/blender_engine.enum";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  placeholder: any = "assets/images/placeholder.svg";
  dtTrigger: Subject<any> = new Subject();
  nodesReady: boolean = false;
  projects: Project[] = [];
  projectLoadComplete: boolean = false;
  projectDetails: Project;
  availableBlenderVersions: any[];
  formats = RenderOutputFormat;
  projectTypes = ProjectType;
  computeMethods = ComputeMethod;
  engines = BlenderEngine;


  constructor(private http: HttpClient, private modalService: NgbModal) {
  }

  ngOnInit() {
    this.getNodeStatus();
    this.getProjectList();
    this.getAvailableBlenderVersions()
    let timer = Observable.timer(5000, 2000);
    timer.subscribe(() => {
      this.getNodeStatus();
      this.getProjectList();
    });
  }

  getAvailableBlenderVersions() {
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions: any[]) => {
          this.availableBlenderVersions = blenderVersions;
        });
  }

  getProjectList() {
    this.http.get('/api/project_ui/project_list').subscribe((projects: Project[]) => {
      this.projects = projects;
      this.projectLoadComplete = true;
    });
  }

  getNodeStatus() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });
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
