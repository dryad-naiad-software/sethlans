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
import {NodeInfo} from "../../../../models/node_info.model";
import {Router} from "@angular/router";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Component({
  selector: 'app-node-scan',
  templateUrl: './node-scan.component.html',
  styleUrls: ['./node-scan.component.scss']
})
export class NodeScanComponent implements OnInit {
  nodeScanComplete: boolean = false;
  scanList: NodeInfo[];
  selectedNodeIP: string[] = [];
  dtOptions: DataTables.Settings = {};



  constructor(private http: HttpClient, private router: Router) {
  }

  ngOnInit() {
    this.scanNode();
    this.dtOptions = {
      ordering: false
    };

  }

  scanNode() {
    this.scanList = [];
    this.nodeScanComplete = false;
    this.http.get('/api/management/node_scan').subscribe((scanList: NodeInfo[]) => {
      this.scanList = scanList;
      this.nodeScanComplete = true;
    })
  }

  returnToNodes(): void {
    this.router.navigateByUrl("/admin/nodes");
  }

  addSelectedNodes() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/setup/multi_node_add', JSON.stringify(this.selectedNodeIP), httpOptions).subscribe((success: boolean) => {
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

}
