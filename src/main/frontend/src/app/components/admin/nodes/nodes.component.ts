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
import {HttpClient} from "@angular/common/http";
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


  dtOptions: DataTables.Settings = {};


  constructor(private http: HttpClient, private router: Router, private nodeListService: NodeListService) {
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

  deleteNode(id) {
    this.http.get('/api/setup/node_delete/' + id + "/", {responseType: 'text'}).subscribe((success: any) => {
      console.log(success);
      this.reload();

    });
  }

  reload(): void {
    this.router.navigateByUrl("/admin/nodes").then(() => location.reload());
  }


}
