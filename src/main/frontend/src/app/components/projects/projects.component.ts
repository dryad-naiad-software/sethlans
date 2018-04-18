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
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {Project} from "../../models/project.model";
import {ProjectListService} from "../../services/project_list.service";
import {Router} from "@angular/router";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit, AfterViewInit {
  placeholder: any = "assets/images/placeholder.svg";
  nodesReady: boolean = false;
  projectSize: number;
  projects: Project[];
  selectedProject: Project;
  dtOptions: DataTables.Settings = {};

  constructor(private http: HttpClient, private projectService: ProjectListService, private router: Router, private modalService: NgbModal) {
  }

  ngAfterViewInit(): void {
    this.projectService.getProjectList().subscribe(value => {
      this.projects = value;
    });
  }

  ngOnInit() {
    this.getNodeStatus();
    this.getProjectListSize();
    this.dtOptions = {
    };


    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getNodeStatus();
      this.getProjectListSize();
    });

    let timer2 = Observable.timer(45000, 30000);
    timer2.subscribe(() => {
      this.reload();
    })
  }

  getProjectListSize() {
    this.projectService.getProjectListSize().subscribe(value => this.projectSize = value);
  }


  getNodeStatus() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });
  }


  reload(): void {
    window.location.href = "/projects";
  }

  addProject() {
    window.location.href = "/projects/add";
  }

  editProject(id) {
    window.location.href = "/projects/edit/" + id;
  }

  viewProject(id) {
    window.location.href = "/projects/view/" + id;
  }

  downloadProject(id) {
    window.location.href = "/api/project_actions/download_project/" + id;
  }


  confirm(project, content) {
    this.selectedProject = project;
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }

  startProject(id) {
    this.http.get("/api/project_actions/start_project/" + id + "/").subscribe((success: boolean) => {
      if (success) {
        window.location.href = "/projects";
      }
    });
  }

  deleteProject(id) {
    this.http.get('/api/project_actions/delete_project/' + id + '/').subscribe();
    window.location.href = "/projects";
  }

  pauseProject(id) {
    this.http.get('/api/project_actions/pause_project/' + id + '/').subscribe();
    window.location.href = "/projects";
  }

  resumeProject(id) {
    this.http.get('/api/project_actions/resume_project/' + id + '/').subscribe();
    window.location.href = "/projects";
  }

  stopProject(id) {
    this.http.get('/api/project_actions/stop_project/' + id + '/').subscribe();
    setTimeout(() => {
      window.location.href = "/projects";
    }, 3000);


  }



}
