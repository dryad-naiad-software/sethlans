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
import Utils from "../../../utils/utils";
import {NodeInfo} from "../../../models/node_info.model";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

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
  inActiveList: boolean[] = [];
  pendingList: boolean[] = [];
  disabledList: boolean[] = [];
  selectedNode: NodeInfo;


  constructor(private http: HttpClient, private nodeListService: NodeListService, private modalService: NgbModal) {
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
    });
    this.nodeListService.getUpdatingNodeList().subscribe(value => {
      let newInActiveList: boolean[] = [];
      let newPendingList: boolean[] = [];
      let newDisabledList: boolean[] = [];
      for (let i = 0; i < value.length; i++) {
        newInActiveList.push(value[i].active);
        newPendingList.push(value[i].benchmarkComplete);
        newDisabledList.push(value[i].disabled);
      }
      if (!Utils.isEqual(newInActiveList, this.inActiveList)) {
        this.loadTable();
      }

      if (!Utils.isEqual(newPendingList, this.pendingList)) {
        this.loadTable();
      }
      if (!Utils.isEqual(newDisabledList, this.disabledList)) {
        this.loadTable();
      }

      this.inActiveList = newInActiveList;
      this.pendingList = newPendingList;
      this.disabledList = newDisabledList;
    })
  }

  loadTable() {
    this.nodeListService.getNodeList().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    })
  }

  confirm(node, content) {
    this.selectedNode = node;
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }

  deleteNode(id) {
    document.getElementById('delete' + id).setAttribute("disabled", "disabled");
    this.http.get('/api/setup/node_delete/' + id + "/").subscribe(() => {
      this.loadTable();
    });
  }

  updateNode(id) {
    document.getElementById('update' + id).setAttribute("disabled", "disabled");
    this.http.get('/api/setup/node_update/' + id + "/").subscribe(() => {
      this.loadTable();
      document.getElementById('update' + id).removeAttribute("disabled");

    });
  }

  addNode() {
    window.location.href = "/admin/nodes/add";
  }

  scanNode() {
    window.location.href = "/admin/nodes/scan";
  }


  enableNode(id) {
    this.http.get('/api/setup/node_enable/' + id + "/").subscribe(() => {
      this.loadTable();
      document.getElementById('disable' + id).removeAttribute("disabled");
    });
  }

  disableNode(id) {
    document.getElementById('disable' + id).setAttribute("disabled", "disabled");
    this.http.get('/api/setup/node_disable/' + id + "/").subscribe(() => {
      setTimeout(() => {
        this.loadTable();
      }, 3000);
    });

  }
}
