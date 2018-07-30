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

import {Component, OnInit, ViewChild} from '@angular/core';
import {ComputeMethod} from '../../../../enums/compute.method.enum';
import {NodeInfo} from '../../../../models/node_info.model';
import {HttpClient} from '@angular/common/http';
import {MatPaginator, MatTableDataSource} from '@angular/material';

@Component({
  selector: 'app-node-add',
  templateUrl: './node-add.component.html',
  styleUrls: ['./node-add.component.scss']
})
export class NodeAddComponent implements OnInit {
  @ViewChild(MatPaginator) nodeListPaginator: MatPaginator;
  nodeListDataSource = new MatTableDataSource();
  nodeListDisplayedColumns = ['ipAddress', 'port'];
  multipleNodes: NodeItem[] = [];
  nodeItem: NodeItem;
  nodeToAdd: NodeInfo;
  nodesToAdd: NodeInfo[] = [];
  summaryComplete: boolean = false;
  computeMethod: any = ComputeMethod;
  connectionID: string;
  accessKey: string;
  wizardModes: any = NodeWizardMode;
  wizardTypes: any = NodeAddType;
  addType: NodeAddType;
  currentMode: NodeWizardMode;
  keyPresent: boolean;
  downloadComplete: boolean;
  multipleNodeAdd: boolean = false;
  tableList: boolean = false;

  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.nodeToAdd = new NodeInfo();
    this.currentMode = NodeWizardMode.Start;
    this.nodeItem = new NodeItem();
  }

  ngOnInit() {
    this.http.get('/api/management/get_key_from_server', {responseType: 'text'}).subscribe((key: string) => this.accessKey = key);
  }

  copyKey() {
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.accessKey;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

  returnToNodes() {
    window.location.href = '/admin/nodes';
  }

  clearNode() {
    this.nodeItem = new NodeItem();
    this.summaryComplete = false;
  }

  clearList() {
    this.multipleNodes = [];
    this.nodeListDataSource = new MatTableDataSource<any>(this.multipleNodes);
    this.nodeListDataSource.paginator = this.nodeListPaginator;
  }


  previous() {
    switch (this.currentMode) {
      case NodeWizardMode.Add:
        this.currentMode = NodeWizardMode.Start;
        break;
      case NodeWizardMode.Summary:
        this.currentMode = NodeWizardMode.Add;

    }
  }

  addNodeToList() {
    this.multipleNodes.push(this.nodeItem);
    this.clearNode();
    this.nodeListDataSource = new MatTableDataSource<any>(this.multipleNodes);
    this.nodeListDataSource.paginator = this.nodeListPaginator;
  }

  queryNode() {
    if (this.multipleNodeAdd) {
      this.multiNodeQuery();
    } else {
      this.singleNodeQuery();
    }
  }

  multiNodeQuery() {
    this.currentMode = NodeWizardMode.Summary;
    this.summaryComplete = false;
    this.downloadComplete = false;
    this.multipleNodes.forEach((value, idx, array) => {
      this.http.get('/api/management/is_key_present?ip=' + value.ipAddress + '&port=' + value.port).subscribe((result: boolean) => {
        if (result) {
          this.http.get('/api/management/node_check?ip=' + value.ipAddress + '&port=' + value.port).subscribe((node: NodeInfo) => {
            if (node != null) {
              this.nodesToAdd.push(node);
              value.active = true;

            } else {
              value.active = false;
            }
            if (idx === array.length - 1) {
              this.summaryComplete = true;
            }
          });
        } else {
          value.active = false;
          if (idx === array.length - 1) {
            this.summaryComplete = true;
          }
        }
      });
    });


  }

  singleNodeQuery() {
    this.currentMode = NodeWizardMode.Summary;
    this.summaryComplete = false;
    this.downloadComplete = false;
    this.http.get('/api/management/is_key_present?ip=' + this.nodeItem.ipAddress + '&port=' + this.nodeItem.port).subscribe((value: boolean) => {
      this.keyPresent = value;
      console.log(value);
      if (value) {
        this.http.get('/api/management/node_check?ip=' + this.nodeItem.ipAddress + '&port=' + this.nodeItem.port).subscribe((node: NodeInfo) => {
          if (node != null) {
            this.nodeToAdd = node;
            this.summaryComplete = true;
            this.downloadComplete = true;
          } else {
            this.summaryComplete = true;
            this.downloadComplete = false;
          }
        });
      } else {
        this.summaryComplete = true;
      }
    });

  }

  addNode() {
    this.http.get('/api/setup/node_add?ip=' + this.nodeItem.ipAddress + '&port=' + this.nodeItem.port, {responseType: 'text'}).subscribe((connectionID: string) => {
      this.connectionID = connectionID;
    });
  }

}

class NodeItem {
  ipAddress: string;
  port: string;
  active: boolean;

  constructor() {
    this.ipAddress = '';
    this.port = '';
    this.active = false;

  }

  nodeItemNotReady(): boolean {
    if (this.ipAddress === '' || this.port === '') {
      return true;
    } else {
      return false;
    }
  }
}

enum NodeAddType {
  Manual,
  Scan,
}

enum NodeWizardMode {
  Start,
  Add,
  Summary,
  Finished
}
