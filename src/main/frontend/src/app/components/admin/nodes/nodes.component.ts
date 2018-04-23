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

import {Component, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {NodeListService} from "../../../services/node_list.service";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.scss']
})
export class NodesComponent implements OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['nodeStatus', 'hostname', 'ipAddress', 'port', 'os', 'computeMethods', 'cpuName', 'selectedCores', 'selectedGPUs', 'benchmark', 'actions'];
  nodeListSize: number;


  constructor(private http: HttpClient, private nodeListService: NodeListService) {
  }

  ngOnInit() {
    this.getInfo();
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => this.getInfo());

  }

  getInfo() {
    this.nodeListService.getNodeListSize().subscribe(value => {
      if (this.nodeListSize != value) {
        this.loadTable();
      }
      this.nodeListSize = value;
    })
  }

  loadTable() {
    this.nodeListService.getNodeList().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    })
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }


  deleteNode(id) {
    this.http.get('/api/setup/node_delete/' + id + "/", {responseType: 'text'}).subscribe((success: any) => {
    });
  }

  addNode() {
    window.location.href = "/admin/nodes/add";
  }

  scanNode() {
    window.location.href = "/admin/nodes/scan";
  }


}
