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

import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {ProjectListService} from '../../../services/project_list.service';
import {HttpClient} from '@angular/common/http';
import {Mode} from '../../../enums/mode.enum';
import {Observable} from 'rxjs/Observable';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {ProjectStatus} from '../../../enums/project_status.enum';
import Utils from '../../../utils/utils';

@Component({
  selector: 'app-server-screen',
  templateUrl: './server-screen.component.html',
  styleUrls: ['./server-screen.component.scss']
})
export class ServerScreenComponent implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['projectName', 'projectStatus', 'progress'];
  projectSize: number;
  chartData: any;
  chartDataArray: number [];
  totalNodes: number;
  activeNodes: number;
  inactiveNodes: number;
  disabledNodes: number;
  totalSlots: number;
  cpuName: string;
  totalMemory: string;
  freeSpace: number;
  totalSpace: number;
  usedSpace: number;
  currentMode: Mode;
  mode: any = Mode;
  currentPercentageArray: number[] = [];
  currentStatusArray: ProjectStatus[] = [];


  constructor(private projectService: ProjectListService, private http: HttpClient) {

  }

  ngOnInit() {
    this.getInfo();
    this.projectLoad();
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getInfo();
    });
  }


  getInfo() {
    this.projectService.getProjectListSize().subscribe(value => {
      if (this.projectSize != value) {
        this.projectLoad();
      }
      this.projectSize = value;
    });
    this.http.get('/api/info/sethlans_mode')
      .subscribe((sethlansmode) => {
        this.currentMode = sethlansmode['mode'];
      });
    this.http.get('/api/info/total_memory', {responseType: 'text'}).subscribe((memory: string) => {
      this.totalMemory = memory;
    });

    this.http.get('/api/info/cpu_name', {responseType: 'text'}).subscribe((cpuName: string) => {
      this.cpuName = cpuName;
    });
    this.http.get('/api/info/total_nodes').subscribe((totalNodes: number) => {
      this.totalNodes = totalNodes;

    });
    this.http.get('/api/info/server_free_space').subscribe((freespace: number) => {
      this.freeSpace = freespace;
    });

    this.http.get('/api/info/server_total_space').subscribe((totalspace: number) => {
      this.totalSpace = totalspace;
    });

    this.http.get('/api/info/server_used_space').subscribe((usedspace: number) => {
      this.usedSpace = usedspace;
    });
    this.http.get('/api/info/active_nodes').subscribe((activeNodes: number) => {
      if (this.activeNodes != activeNodes) {
        this.chartLoad();
      }
      this.activeNodes = activeNodes;
    });
    this.http.get('/api/info/disabled_nodes').subscribe((disabledNodes: number) => {
      this.disabledNodes = disabledNodes;
    });
    this.http.get('/api/info/inactive_nodes').subscribe((inactiveNodes: number) => {
      this.inactiveNodes = inactiveNodes;
    });

    this.http.get('/api/info/server_total_slots').subscribe((totalSlots: number) => {
      this.totalSlots = totalSlots;
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


  chartLoad() {
    this.http.get('/api/info/active_nodes_value_array').subscribe((numberArray: number[]) => {
      this.chartDataArray = numberArray;
      this.chartData = {
        labels: ['CPU', 'GPU', 'CPU_GPU'],
        datasets: [
          {
            data: this.chartDataArray,
            backgroundColor: [
              '#43C519',
              '#1943C5',
              '#C51943'
            ],
            hoverBackgroundColor: [
              '#43C519',
              '#1943C5',
              '#C51943'
            ]
          }]
      };
    });
  }

  ngAfterViewInit(): void {
  }


}
