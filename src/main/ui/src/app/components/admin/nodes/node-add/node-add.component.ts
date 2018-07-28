/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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
import {ComputeMethod} from '../../../../enums/compute.method.enum';
import {NodeInfo} from '../../../../models/node_info.model';
import {HttpClient} from '@angular/common/http';

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
  computeMethod: any = ComputeMethod;
  connectionID: string;
  accessKey: string;
  wizardModes: any = NodeWizardMode;
  selectedMode: NodeWizardMode;
  currentMode: NodeWizardMode;
  keyPresent: boolean;

  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.nodeToAdd = new NodeInfo();
    this.currentMode = NodeWizardMode.Start;
  }

  ngOnInit() {
    this.http.get('/api/management/get_key_from_server', {responseType: 'text'}).subscribe((key: string) => this.accessKey = key);
  }

  setWizardMode() {
    this.currentMode = this.selectedMode;
  }

  verifyNode() {
    this.http.get('/api/management/is_key_present?ip=' + this.ipAddress + '&port=' + this.port).subscribe((value: boolean) => {
      this.keyPresent = value;
      console.log(value);
      if (value) {
        this.http.get('/api/management/node_check?ip=' + this.ipAddress + '&port=' + this.port).subscribe((node: NodeInfo) => {
          if (node != null) {
            this.nodeToAdd = node;
          }
        });
      }
    });

  }

  addNode() {
    this.http.get('/api/setup/node_add?ip=' + this.ipAddress + '&port=' + this.port, {responseType: 'text'}).subscribe((connectionID: string) => {
      this.connectionID = connectionID;
    });
  }

}

enum NodeWizardMode {
  Start,
  Manual,
  Scan
}
