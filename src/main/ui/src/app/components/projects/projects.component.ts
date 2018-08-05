/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import {Component, OnInit, ViewChild} from '@angular/core';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {ProjectStatus} from '../../enums/project_status.enum';
import Utils from '../../utils/utils';
import {timer} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {ProjectListService} from '../../services/project_list.service';
import {Project} from '../../models/project.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['projectName', 'blender', 'type', 'renderOn', 'resolution', 'format', 'projectStatus', 'progress', 'preview', 'actions'];
  placeholder: any = 'assets/images/placeholder.svg';
  nodesReady: boolean = false;
  projectSize: number;
  selectedProject: Project;
  currentPercentageArray: number[] = [];
  currentStatusArray: ProjectStatus[] = [];

  constructor(private http: HttpClient, private projectService: ProjectListService, private router: Router, private modalService: NgbModal) {
  }


  ngOnInit() {
    this.getInfo();
    let scheduler = timer(1000, 1000);
    scheduler.subscribe(() => {
      this.getInfo();
    });

  }

  getInfo() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });

    this.projectService.getProjectListSize().subscribe(value => {
      if (this.projectSize != value) {
        this.projectLoad();
      }
      this.projectSize = value;
    });

    this.projectService.getProjectListInProgress().subscribe(value => {
      let newPercentageArray: number[] = [];
      let newStatusArray: ProjectStatus[] = [];
      for (let i = 0; i < value.length; i++) {
        newPercentageArray.push(value[i].currentPercentage);
        newStatusArray.push(value[i].projectStatus);
      }
      if (!Utils.isEqual(newPercentageArray, this.currentPercentageArray)) {
        this.projectLoad();
      }

      if (!Utils.isEqual(newStatusArray, this.currentStatusArray)) {
        this.projectLoad();
      }

      this.currentPercentageArray = newPercentageArray;
      this.currentStatusArray = newStatusArray;
    });
  }


  projectLoad() {
    this.projectService.getProjectList().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  addProject() {
    window.location.href = '/projects/add';
  }

  editProject(id) {
    window.location.href = '/projects/edit/' + id;
  }

  viewProject(id) {
    window.location.href = '/projects/view/' + id;
  }

  downloadProject(id) {
    window.location.href = '/api/project_actions/download_project/' + id;
  }

  downloadVideo(id) {
    window.location.href = '/api/project_actions/download_project_video/' + id;
  }


  confirm(project, content) {
    this.selectedProject = project;
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }

  startProject(id) {
    this.http.get('/api/project_actions/start_project/' + id + '/').subscribe((success: boolean) => {
      if (success) {
        this.projectLoad();
      }
    });
  }

  deleteProject(id) {
    this.http.get('/api/project_actions/delete_project/' + id + '/').subscribe();
  }

  pauseProject(id) {
    this.http.get('/api/project_actions/pause_project/' + id + '/').subscribe();
    this.projectLoad();
  }

  resumeProject(id) {
    this.http.get('/api/project_actions/resume_project/' + id + '/').subscribe();
    this.projectLoad();
  }

  stopProject(id) {
    this.http.get('/api/project_actions/stop_project/' + id + '/').subscribe();
  }

}
