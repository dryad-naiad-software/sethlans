/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NodeWizardForm} from '../../../../../models/forms/node_wizard_form.model';
import {NodeInfo} from '../../../../../models/node_info.model';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {NodeWizardProgress} from '../../../../../enums/node_wizard_progress.enum';
import {NodeAddType} from '../../../../../enums/node_wizard_add_type.enum';
import {NodeItem} from '../../../../../models/node_item.model';
import {Login} from '../../../../../models/login.model';
import {ComputeMethod} from '../../../../../enums/compute.method.enum';

@Component({
  selector: 'app-node-summary',
  templateUrl: './node-summary.component.html',
  styleUrls: ['./node-summary.component.scss']
})
export class NodeSummaryComponent implements OnInit, AfterViewInit {
  @Input() nodeWizardForm: NodeWizardForm;
  @Input() accessKey: string;
  wizardModes: any = NodeWizardProgress;
  computeTypes: any = ComputeMethod;
  keyPresent: boolean;
  downloadComplete: boolean;
  @Output() disableNext = new EventEmitter();
  @ViewChild(MatPaginator) obtainedNodePaginator: MatPaginator;
  @ViewChild(MatSort) obtainedNodeSort: MatSort;
  obtainedNodeDataSource = new MatTableDataSource();
  obtainedNodeDisplayedColumns = ['hostname', 'ipAddress', 'port', 'os', 'computeMethods', 'cpuName', 'selectedCores', 'selectedGPUs'];
  wizardTypes: any = NodeAddType;
  jsonToSend: JsonToSend;



  constructor(private http: HttpClient) {
    this.downloadComplete = false;
    this.jsonToSend = new JsonToSend();
  }

  ngOnInit() {
    if (this.nodeWizardForm.addType == NodeAddType.Manual) {
      if (!this.nodeWizardForm.dontUseAuth) {
        this.submitAuth();
        let wizard = this;
        setTimeout(function () {
          wizard.multiNodeQuery();
        }, wizard.nodeWizardForm.listOfNodes.length * 500);
      } else {
        this.multiNodeQuery();
      }
    } else {
      this.scannedSummary();
    }
  }

  multiNodeQuery() {
    this.nodeWizardForm.listOfNodes.forEach((value, idx, array) => {
      this.http.get('/api/management/node_check?ip=' + value.ipAddress + '&port=' + value.port).subscribe((node: NodeInfo) => {
        if (node != null) {
          this.nodeWizardForm.nodesToAdd.push(node);
          value.active = true;
        } else {
          value.active = false;
        }
        if (idx === array.length - 1) {
          let wizard = this;
          setTimeout(function () {
            wizard.refreshList();
            wizard.downloadComplete = true;
            wizard.disableNext.emit(false);
          }, array.length * 500);
        }
      });
    });
  }

  refreshList() {
    this.obtainedNodeDataSource = new MatTableDataSource<any>(this.nodeWizardForm.nodesToAdd);
    this.obtainedNodeDataSource.paginator = this.obtainedNodePaginator;
    this.obtainedNodeDataSource.sort = this.obtainedNodeSort;
  }

  scannedSummary() {
    this.obtainedNodeDataSource = new MatTableDataSource<any>(this.nodeWizardForm.nodesToAdd);
    this.obtainedNodeDataSource.sort = this.obtainedNodeSort;
    this.disableNext.emit(false);
  }


  submitAuth() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.jsonToSend.listOfNodes = this.nodeWizardForm.listOfNodes;
    this.jsonToSend.login = this.nodeWizardForm.nodeLogin;
    this.http.post('/api/management/server_to_node_auth', JSON.stringify(this.jsonToSend), httpOptions).subscribe(() => {
    });
  }


  ngAfterViewInit(): void {
    this.obtainedNodeDataSource.paginator = this.obtainedNodePaginator;
  }

}

class JsonToSend {
  listOfNodes: NodeItem[];
  login: Login;

}
