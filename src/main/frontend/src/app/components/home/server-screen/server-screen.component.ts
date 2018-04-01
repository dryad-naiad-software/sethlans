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
import {ProjectListService} from "../../../services/project_list.service";
import {Project} from "../../../models/project.model";
import {Subject} from "rxjs/Subject";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-server-screen',
  templateUrl: './server-screen.component.html',
  styleUrls: ['./server-screen.component.scss']
})
export class ServerScreenComponent implements OnInit, AfterViewInit {
  projects: Project[];
  projectSize: number;
  dtTrigger: Subject<any> = new Subject();
  dtOptions: DataTables.Settings = {};
  data: any;
  dataArray: number [];
  totalNodes: number;
  activeNodes: number;
  inactiveNodes: number;
  disabledNodes: number;


  constructor(private projectService: ProjectListService, private http: HttpClient) {

  }

  ngOnInit() {
    this.dtOptions = {
      searching: false
    };
    this.http.get('/api/info/total_nodes').subscribe((totalNodes: number) => {
      this.totalNodes = totalNodes;
    });
    this.http.get('/api/info/active_nodes').subscribe((activeNodes: number) => {
      this.activeNodes = activeNodes;
    });
    this.http.get('/api/info/disabled_nodes').subscribe((disabledNodes: number) => {
      this.disabledNodes = disabledNodes;
    });
    this.http.get('/api/info/inactive_nodes').subscribe((inactiveNodes: number) => {
      this.inactiveNodes = inactiveNodes;
    });


    this.http.get('/api/info/active_nodes_value_array').subscribe((numberArray: number[]) => {
      this.dataArray = numberArray;
      this.data = {
        labels: ['CPU', 'GPU', 'CPU_GPU'],
        datasets: [
          {
            data: this.dataArray,
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
    });


    this.projectService.getProjectListSize().subscribe(value => this.projectSize = value);
    this.projectService.getProjectList().subscribe(value => {
      this.projects = value;
      this.dtTrigger.next();
    });
  }

  ngAfterViewInit(): void {


  }


}
