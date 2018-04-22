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
import {ProjectListService} from "../../../services/project_list.service";
import {Project} from "../../../models/project.model";
import {Subject} from "rxjs/Subject";
import {HttpClient} from "@angular/common/http";
import {Mode} from "../../../enums/mode.enum";
import {Observable} from "rxjs/Observable";
import {DataTableDirective} from "angular-datatables";
import {ProjectStatus} from "../../../enums/project_status.enum";

@Component({
  selector: 'app-server-screen',
  templateUrl: './server-screen.component.html',
  styleUrls: ['./server-screen.component.scss']
})
export class ServerScreenComponent implements OnInit, AfterViewInit {
  @ViewChild(DataTableDirective)
  dtElement: DataTableDirective;

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
    this.dtOptions = {
      searching: false,
      pageLength: 5,
      lengthChange: false
    };
    this.getInfo();
    this.chartLoad();
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getInfo();
    });

    this.projectService.getProjectList().subscribe(value => {
      this.projects = value;
      for (let i = 0; i < value.length; i++) {
        this.currentPercentageArray.push(value[i].currentPercentage);
        this.currentStatusArray.push(value[i].projectStatus);
      }
      this.dtTrigger.next();
    });
  }

  ngAfterViewInit(): void {

  }

  getInfo() {
    this.http.get('/api/info/sethlans_mode', {responseType: 'text'})
      .subscribe((sethlansmode: Mode) => {
        this.currentMode = sethlansmode;
      });
    this.http.get('/api/info/total_memory', {responseType: 'text'}).subscribe((memory: string) => {
      this.totalMemory = memory;
    });

    this.http.get('/api/info/cpu_name', {responseType: 'text'}).subscribe((cpuName: string) => {
      this.cpuName = cpuName;
    });
    this.http.get('/api/info/total_nodes').subscribe((totalNodes: number) => {
      if (this.totalNodes != totalNodes) {
        this.chartLoad();
      }
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

    this.http.get('/api/info/total_slots').subscribe((totalSlots: number) => {
      this.totalSlots = totalSlots;
    });

    this.projectService.getProjectListSize().subscribe(value => {
      if (this.projectSize != value) {
        this.loadTable();
      }
      this.projectSize = value
    });

    this.projectService.getProjectList().subscribe(value => {
      let newPercentageArray: number[] = [];
      let newStatusArray: ProjectStatus[] = [];
      for (let i = 0; i < value.length; i++) {
        newPercentageArray.push(value[i].currentPercentage);
        newStatusArray.push(value[i].projectStatus);
      }
      if (!this.isEqual(newPercentageArray, this.currentPercentageArray)) {
        this.loadTable();
      }

      if (!this.isEqual(newStatusArray, this.currentStatusArray)) {
        this.loadTable();
      }

      this.currentPercentageArray = newPercentageArray;
      this.currentStatusArray = newStatusArray;
    });
  }

  isEqual(value, other) {
    // Get the value type
    var type = Object.prototype.toString.call(value);

    // If the two objects are not the same type, return false
    if (type !== Object.prototype.toString.call(other)) return false;

    // If items are not an object or array, return false
    if (['[object Array]', '[object Object]'].indexOf(type) < 0) return false;

    // Compare the length of the length of the two items
    var valueLen = type === '[object Array]' ? value.length : Object.keys(value).length;
    var otherLen = type === '[object Array]' ? other.length : Object.keys(other).length;
    if (valueLen !== otherLen) return false;

    // Compare two items
    var compare = function (item1, item2) {

      // Get the object type
      var itemType = Object.prototype.toString.call(item1);

      // If an object or array, compare recursively
      if (['[object Array]', '[object Object]'].indexOf(itemType) >= 0) {
        if (!isEqual(item1, item2)) return false;
      }

      // Otherwise, do a simple comparison
      else {

        // If the two items are not the same type, return false
        if (itemType !== Object.prototype.toString.call(item2)) return false;

        // Else if it's a function, convert to a string and compare
        // Otherwise, just compare
        if (itemType === '[object Function]') {
          if (item1.toString() !== item2.toString()) return false;
        } else {
          if (item1 !== item2) return false;
        }

      }
    };

    // Compare properties
    if (type === '[object Array]') {
      for (var i = 0; i < valueLen; i++) {
        if (compare(value[i], other[i]) === false) return false;
      }
    } else {
      for (var key in value) {
        if (value.hasOwnProperty(key)) {
          if (compare(value[key], other[key]) === false) return false;
        }
      }
    }

    // If nothing failed, return true
    return true;
  }

  loadTable() {
    this.projectService.getProjectList().subscribe(value => {
      this.dtElement.dtInstance.then((dtInstance: DataTables.Api) => {
        // Destroy the table first
        dtInstance.destroy();
        this.projects = value;
        // Call the dtTrigger to rerender again
        this.dtTrigger.next();

      });
    });
  }

  chartLoad() {
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
  }


}
