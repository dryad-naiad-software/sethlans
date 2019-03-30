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

import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ServerDashboard} from '../../../models/server_dash.model';
import {HttpClient} from '@angular/common/http';
import {ProjectListService} from '../../../services/project_list.service';
import {MatSort, MatTableDataSource} from '@angular/material';
import {ProjectStatus} from '../../../enums/project_status.enum';
import {Mode} from '../../../enums/mode.enum';
import {timer} from 'rxjs/internal/observable/timer';
import Utils from '../../../utils/utils';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {Project} from '../../../models/project.model';

@Component({
  selector: 'app-server-screen',
  templateUrl: './server-screen.component.html',
  styleUrls: ['./server-screen.component.scss']
})
export class ServerScreenComponent implements OnInit {
  serverDash: ServerDashboard;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['projectName', 'projectStatus', 'progress', 'actions'];
  projectStatus: any = ProjectStatus;
  mode: any = Mode;
  projectSize: number;
  activeNodes: number;
  @Input() currentMode: Mode;
  chartData: { labels: string[]; datasets: { data: any; backgroundColor: string[]; hoverBackgroundColor: string[] }[] };
  currentPercentageArray: number[];
  currentStatusArray: ProjectStatus[];
  selectedProject: Project;


  constructor(private http: HttpClient, private projectService: ProjectListService, private modalService: NgbModal) {
    this.currentPercentageArray = [];
    this.currentStatusArray = [];
    this.serverDash = new ServerDashboard();
    this.activeNodes = 0;
  }

  ngOnInit() {
    this.getInfo();
    this.projectLoad();
    let scheduler = timer(15000, 15000);
    scheduler.subscribe(() => this.getInfo())

  }

  getInfo() {
    this.projectService.getProjectListSize().subscribe(value => {
      if (this.projectSize != value) {
        this.projectLoad();
      }
      this.projectSize = value
    });
    this.http.get('/api/info/server_dashboard').subscribe((serverDashboard: ServerDashboard) => {
      this.serverDash = serverDashboard;
      if (this.activeNodes != this.serverDash.activeNodes) {
        this.chartLoad();
      }
      this.activeNodes = this.serverDash.activeNodes;
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
    this.projectService.getLastFive().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.sort = this.sort;
    });
  }

  confirm(project, content) {
    this.selectedProject = project;
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }

  deleteProject(id) {
    this.http.get('/api/project_actions/delete_project/' + id + '/').subscribe();
  }

  stopProject(id) {
    this.http.get('/api/project_actions/stop_project/' + id + '/').subscribe();
  }

  chartLoad() {
    this.chartData = {
      labels: ['CPU', 'GPU', 'CPU_GPU'],
      datasets: [
        {
          data: this.serverDash.numberOfActiveNodesArray,
          backgroundColor: [
            "#43C519",
            "#1943C5",
            "#C51943"
          ],
          hoverBackgroundColor: [
            "#43C519",
            "#1943C5",
            "#C51943"
          ]
        }]
    };
  }
}
