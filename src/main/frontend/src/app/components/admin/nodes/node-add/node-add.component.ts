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
import {NodeInfo} from '../../../../models/node_info.model';
import {ComputeMethod} from '../../../../enums/compute.method.enum';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';

@Component({
  selector: 'app-node-add',
  templateUrl: './node-add.component.html',
  styleUrls: ['./node-add.component.scss']
})
export class NodeAddComponent implements OnInit {
  ipAddress: string;
  port: string;
  nodeToAdd: NodeInfo;
  summaryComplete: boolean = false;
  computeMethodEnum: any = ComputeMethod;
  formSubmitted: boolean = false;
  connectionID: string;


  constructor(private http: HttpClient, private router: Router) {
  }

  ngOnInit() {
  }

  verifyNode() {
    this.formSubmitted = true;
    this.http.get('/api/management/node_check?ip=' + this.ipAddress + '&port=' + this.port).subscribe((node: NodeInfo) => {
      this.nodeToAdd = node;
      this.summaryComplete = true;
    });
  }

  addNode() {
    this.http.get('/api/setup/node_add?ip=' + this.ipAddress + '&port=' + this.port, {responseType: 'text'}).subscribe((connectionID: string) => {
      this.connectionID = connectionID;
      this.activateNode();
    });
  }

  activateNode() {
    this.http.get('/api/setup/auto_acknowledge/' + this.connectionID + '/').subscribe(() => {
      this.returnToNodes();
    });

  }

  returnToNodes(): void {
    window.location.href = '/admin/nodes';
  }

}
