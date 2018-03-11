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

import {Component, OnInit} from '@angular/core';
import {Subject} from "rxjs/Subject";
import {NodeInfo} from "../../../models/node_info.model";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";
import {HttpClient} from "@angular/common/http";
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {Router} from "@angular/router";

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.scss']
})
export class NodesComponent implements OnInit {
  dtTrigger: Subject<any> = new Subject();
  nodeList: NodeInfo[] = [];
  ipAddress: string;
  port: string;
  nodeToAdd: NodeInfo;
  computeMethodEnum: any = ComputeMethod;


  constructor(private modalService: NgbModal, private http: HttpClient, private router: Router) {
  }

  ngOnInit() {
    this.http.get('/api/management/node_list')
      .subscribe((nodes: NodeInfo[]) => {
        this.nodeList = nodes;
      });
  }

  openModal(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }

  resetAddNode() {
    this.ipAddress = "";
    this.port = "";
  }

  openNodeSummaryModal(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.http.get('/api/management/node_check?ip=' + this.ipAddress + "&port=" + this.port).subscribe((node: NodeInfo) => {
      this.nodeToAdd = node;
    });
    this.modalService.open(content, options);
  }

  deleteNode(id) {
    this.http.get('/api/setup/node_delete/' + id + "/", {responseType: 'text'}).subscribe((success: any) => {
      console.log(success);
      this.router.navigateByUrl("/admin/nodes").then(() => {
        location.reload();
      });
    });
  }

  addNode() {
    this.http.get('/api/setup/node_add?ip=' + this.ipAddress + "&port=" + this.port, {responseType: 'text'}).subscribe((success: any) => {
      console.log(success);
      this.router.navigateByUrl("/admin/nodes").then(() => {
        location.reload();
      });

    });

  }

  scanNode() {
  }


}
