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
import {GetStartedWizardForm} from '../../../../models/forms/get_started_wizard_form.model';
import {NodeInfo} from '../../../../models/node_info.model';
import {HttpClient} from '@angular/common/http';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {GetStartedProgress} from '../../../../enums/get_started_progress.enum';
import {ComputeMethod} from '../../../../enums/compute.method.enum';

@Component({
  selector: 'app-wizard-add-nodes',
  templateUrl: './wizard-add-nodes.component.html',
  styleUrls: ['./wizard-add-nodes.component.scss']
})
export class WizardAddNodesComponent implements OnInit {
  @Input() getStartedWizardForm: GetStartedWizardForm;
  @Output() disableNext = new EventEmitter();
  @ViewChild(MatPaginator) obtainedNodePaginator: MatPaginator;
  @ViewChild(MatSort) obtainedNodeSort: MatSort;
  wizardProgress: any = GetStartedProgress;
  computeTypes: any = ComputeMethod;
  obtainedNodeDataSource = new MatTableDataSource();
  obtainedNodeDisplayedColumns = ['hostname', 'ipAddress', 'port', 'os', 'computeMethods', 'cpuName', 'selectedCores', 'selectedGPUs'];


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.multiNodeQuery();
  }

  multiNodeQuery() {
    this.getStartedWizardForm.listOfNodes.forEach((value, idx, array) => {
      this.http.get('/api/management/node_check?ip=' + value.ipAddress + '&port=' + value.port).subscribe((node: NodeInfo) => {
        if (node != null) {
          this.getStartedWizardForm.nodesToAdd.push(node);
          value.active = true;
        } else {
          value.active = false;
        }
        if (idx === array.length - 1) {
          this.getStartedWizardForm.scanComplete = true;
          this.refreshList();
        }
      });
    });
  }

  refreshList() {
    this.obtainedNodeDataSource = new MatTableDataSource<any>(this.getStartedWizardForm.nodesToAdd);
    this.obtainedNodeDataSource.paginator = this.obtainedNodePaginator;
    this.obtainedNodeDataSource.sort = this.obtainedNodeSort;
  }


}