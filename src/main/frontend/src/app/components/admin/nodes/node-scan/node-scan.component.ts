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
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {Observable} from "rxjs/Observable";
import {NodeInfo} from "../../../../models/node_info.model";
import {SelectionModel} from "@angular/cdk/collections";

@Component({
  selector: 'app-node-scan',
  templateUrl: './node-scan.component.html',
  styleUrls: ['./node-scan.component.scss']
})
export class NodeScanComponent implements OnInit {
  nodeScanComplete: boolean = false;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  scanSize: number;
  dataSource = new MatTableDataSource();
  displayedColumns = ['selection', 'hostname', 'ipAddress', 'port', 'os', 'computeMethods', 'cpuName', 'selectedCores', 'selectedGPUs'];
  connectionIds: string[];
  selection = new SelectionModel(true, []);

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.loadTable();

  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data.forEach(row => this.selection.select(row));
  }

  loadTable() {
    this.getScannedNodes().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.nodeScanComplete = true;
      this.scanSize = data.length;
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    })
  }


  returnToNodes(): void {
    window.location.href = "/admin/nodes";
  }

  addSelectedNodes() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };

    this.http.post('/api/setup/multi_node_add', JSON.stringify(this.selection), httpOptions).subscribe((connectionIds: string[]) => {
      this.connectionIds = connectionIds;
      this.acknowledgeSelectedNodes()
    });

  }

  acknowledgeSelectedNodes() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };

    this.http.post('/api/setup/multi_auto_acknowledge', JSON.stringify(this.connectionIds), httpOptions).subscribe(() => {
      this.returnToNodes();
    });
  }


  getScannedNodes(): Observable<NodeInfo[]> {
    return this.http.get<NodeInfo[]>('/api/management/node_scan/');
  }

}
