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
  selectedNodeIP: string[] = [];
  connectionIds: string[];


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.loadTable();

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
    this.http.post('/api/setup/multi_node_add', JSON.stringify(this.selectedNodeIP), httpOptions).subscribe((connectionIds: string[]) => {
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

  selectNode(event) {
    var index = this.selectedNodeIP.indexOf(event.target.value);
    if (event.target.checked) {
      this.selectedNodeIP.push(event.target.value);
    } else {
      if (index !== -1) {
        this.selectedNodeIP.splice(index, 1);
      }
    }
    console.log(this.selectedNodeIP);
  }

  getScannedNodes(): Observable<NodeInfo[]> {
    return this.http.get<NodeInfo[]>('/api/management/node_scan/');
  }

}
