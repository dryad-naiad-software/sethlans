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
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-node-scan',
  templateUrl: './node-scan.component.html',
  styleUrls: ['./node-scan.component.scss']
})
export class NodeScanComponent implements OnInit {
  nodeScanComplete: boolean = false;
  scanList: NodeInfo[];


  constructor(private http: HttpClient, private router: Router) {
  }

  ngOnInit() {
    this.scanNode();
  }

  scanNode() {
    this.scanList = [];
    this.nodeScanComplete = false;
    this.http.get('/api/management/node_scan').subscribe((scanList: NodeInfo[]) => {
      this.scanList = scanList;
      this.nodeScanComplete = true;
    })
  }

}
