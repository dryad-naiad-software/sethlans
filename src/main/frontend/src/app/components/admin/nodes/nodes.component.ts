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
import {NodeInfo} from "../../../models/node_info.model";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {HttpClient} from "@angular/common/http";
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";
import {NodeListService} from "../../../services/node_list.service";

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.scss']
})
export class NodesComponent implements OnInit, AfterViewInit {
  nodeListSize: number;
  nodeList: NodeInfo[] = [];
  ipAddress: string;
  port: string;
  nodeToAdd: NodeInfo;
  nodeToEditId: number;
  computeMethodEnum: any = ComputeMethod;
  summaryComplete: boolean = false;
  nodeScanComplete: boolean = false;
  dtOptions: DataTables.Settings = {};


  constructor(private modalService: NgbModal, private http: HttpClient, private router: Router, private nodeListService: NodeListService) {
  }

  ngOnInit() {
    this.dtOptions = {};
    this.getNodeListSize();
    let timer = Observable.timer(60000, 60000);
    timer.subscribe(() => this.reload());
    let timer2 = Observable.timer(5000, 2000);
    timer2.subscribe(() => this.getNodeListSize());
  }

  ngAfterViewInit(): void {
    this.nodeListService.getNodeList().subscribe(value => {
      this.nodeList = value;
    });
  }

  getNodeListSize() {
    this.http.get<number>("/api/management/node_list_size").subscribe((nodeSize: number) => {
      this.nodeListSize = nodeSize;
    });
  }

  openModal(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }


  backModalAdd(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.nodeToAdd = null;
    this.summaryComplete = false;
    this.modalService.open(content, options);
  }

  resetAddNode() {
    this.nodeToEditId = null;
    this.ipAddress = "";
    this.port = "";
    this.nodeToAdd = null;
    this.summaryComplete = false;
  }

  openNodeSummaryModal(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.http.get('/api/management/node_check?ip=' + this.ipAddress + "&port=" + this.port).subscribe((node: NodeInfo) => {
      this.nodeToAdd = node;
      this.summaryComplete = true;
    });
    this.modalService.open(content, options);
  }

  deleteNode(id) {
    this.http.get('/api/setup/node_delete/' + id + "/", {responseType: 'text'}).subscribe((success: any) => {
      console.log(success);
      this.reload();

    });
  }

  updateNode(id, content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.nodeToEditId = id;
    this.http.get('/api/management/node_update_info/' + this.nodeToEditId + "/").subscribe((node: NodeInfo) => {
      this.ipAddress = node.ipAddress;
      this.port = node.networkPort;
      this.modalService.open(content, options);
    })


  }

  addNode() {
    if (this.nodeToEditId == undefined || this.nodeToEditId == null) {
      this.http.get('/api/setup/node_add?ip=' + this.ipAddress + "&port=" + this.port).subscribe((success: boolean) => {
        if (success == true) {
          this.resetAddNode();
          this.reload();
        }
      });
    } else {
      this.http.get('/api/setup/node_edit/' + this.nodeToEditId + '/' + '?ip=' + this.ipAddress + '&port=' + this.port).subscribe((success: boolean) => {
        if (success == true) {
          this.resetAddNode();
          this.reload();
        }
      })
    }

  }

  scanNode() {
  }

  reload(): void {
    this.router.navigateByUrl("/admin/nodes").then(() => location.reload());
  }


}
