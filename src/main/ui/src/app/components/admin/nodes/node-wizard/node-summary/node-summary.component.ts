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

import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NodeWizardForm} from '../../../../../models/forms/node_wizard_form.model';
import {NodeInfo} from '../../../../../models/node_info.model';
import {HttpClient} from '@angular/common/http';
import {MatPaginator, MatTableDataSource} from '@angular/material';
import {NodeWizardMode} from '../../../../../enums/node_wizard_mode.enum';

@Component({
  selector: 'app-node-summary',
  templateUrl: './node-summary.component.html',
  styleUrls: ['./node-summary.component.scss']
})
export class NodeSummaryComponent implements OnInit {
  @Input() nodeWizardForm: NodeWizardForm;
  @Input() accessKey: string;
  wizardModes: any = NodeWizardMode;
  keyPresent: boolean;
  downloadComplete: boolean;
  @Output() disableNext = new EventEmitter();
  @ViewChild(MatPaginator) obtainedNodePaginator: MatPaginator;
  obtainedNodeDataSource = new MatTableDataSource();
  obtainedNodeDisplayedColumns = ['hostname', 'ipAddress', 'port', 'os', 'computeMethods', 'cpuName', 'selectedCores', 'selectedGPUs'];


  constructor(private http: HttpClient) {
    this.downloadComplete = false;
  }

  ngOnInit() {
    if (this.nodeWizardForm.multipleNodeAdd) {
      this.nodeWizardForm.nodesToAdd = [];
      this.multiNodeQuery();
    } else {
      this.nodeWizardForm.nodeToAdd = new NodeInfo();
      this.singleNodeQuery();
    }
  }

  refreshList() {
    if (!this.nodeWizardForm.multipleNodeAdd) {
      let tempTable: NodeInfo[] = [];
      tempTable.push(this.nodeWizardForm.nodeToAdd);
      this.obtainedNodeDataSource = new MatTableDataSource<any>(tempTable);
      this.obtainedNodeDataSource.paginator = this.obtainedNodePaginator;
    } else {
      this.obtainedNodeDataSource = new MatTableDataSource<any>(this.nodeWizardForm.nodesToAdd);
      this.obtainedNodeDataSource.paginator = this.obtainedNodePaginator;
    }
  }

  singleNodeQuery() {
    this.http.get('/api/management/is_key_present?ip=' + this.nodeWizardForm.singleNode.ipAddress + '&port=' + this.nodeWizardForm.singleNode.port).subscribe((value: boolean) => {
      this.keyPresent = value;
      console.log(value);
      if (value) {
        this.http.get('/api/management/node_check?ip=' + this.nodeWizardForm.singleNode.ipAddress + '&port=' + this.nodeWizardForm.singleNode.port).subscribe((node: NodeInfo) => {
          if (node != null) {
            this.nodeWizardForm.nodeToAdd = node;
            this.nodeWizardForm.summaryComplete = true;
            this.downloadComplete = true;
            this.disableNext.emit(false);
            this.refreshList();
          } else {
            this.nodeWizardForm.summaryComplete = true;
            this.downloadComplete = false;
          }
        });
      } else {
        this.nodeWizardForm.summaryComplete = true;
      }
    });
  }

  multiNodeQuery() {
    this.nodeWizardForm.multipleNodes.forEach((value, idx, array) => {
      this.http.get('/api/management/is_key_present?ip=' + value.ipAddress + '&port=' + value.port).subscribe((result: boolean) => {
        if (result) {
          this.http.get('/api/management/node_check?ip=' + value.ipAddress + '&port=' + value.port).subscribe((node: NodeInfo) => {
            if (node != null) {
              this.nodeWizardForm.nodesToAdd.push(node);
              value.active = true;
              this.keyPresent = true;
              this.disableNext.emit(false);
            } else {
              value.active = false;
            }
            if (idx === array.length - 1) {
              this.nodeWizardForm.summaryComplete = true;
              this.downloadComplete = true;
              this.refreshList();
            }
          });
        } else {
          value.active = false;
          if (idx === array.length - 1) {
            this.nodeWizardForm.summaryComplete = true;
            this.downloadComplete = true;
            this.refreshList();
            this.disableNext.emit(true);

          }
        }
      });
    });
  }

}
