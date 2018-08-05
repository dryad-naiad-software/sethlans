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

import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NodeWizardForm} from '../../../../../models/forms/node_wizard_form.model';
import {NodeInfo} from '../../../../../models/node_info.model';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';

@Component({
  selector: 'app-node-scan-add',
  templateUrl: './node-scan-add.component.html',
  styleUrls: ['./node-scan-add.component.scss']
})
export class NodeScanAddComponent implements OnInit, AfterViewInit {
  nodeScanComplete: boolean = false;
  scanSize: number;
  @Input() nodeWizardForm: NodeWizardForm;
  @Output() disableNext = new EventEmitter();
  scanTableDataSource = new MatTableDataSource();
  @ViewChild(MatPaginator) scanTablePaginator: MatPaginator;
  @ViewChild(MatSort) scanTableSort: MatSort;
  selection = new SelectionModel(true, []);
  scanTableDisplayedColumns = ['selection', 'hostname', 'ipAddress', 'port', 'os', 'computeMethods', 'cpuName', 'selectedCores', 'selectedGPUs'];

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.nodeWizardForm.nodesToAdd = [];
    this.loadTable();
    this.disableNext.emit(true);
  }

  loadTable() {
    this.getScannedNodes().subscribe(data => {
      this.scanTableDataSource = new MatTableDataSource<any>(data);
      this.nodeScanComplete = true;
      this.scanSize = data.length;
      this.scanTableDataSource.sort = this.scanTableSort;
      setTimeout(() => this.scanTableDataSource.paginator = this.scanTablePaginator);

    });
  }

  getScannedNodes(): Observable<NodeInfo[]> {
    return this.http.get<NodeInfo[]>('/api/management/node_scan/');
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.scanTableDataSource.data.forEach(row => {
        this.selection.select(row);
      });
    this.updateToSend();
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.scanTableDataSource.data.length;
    return numSelected === numRows;
  }

  updateToSend() {
    this.nodeWizardForm.nodesToAdd = [];
    this.selection.selected.forEach((value: NodeInfo) => {
      this.nodeWizardForm.nodesToAdd.push(value);
    });
    if (this.nodeWizardForm.nodesToAdd.length < 1) {
      this.disableNext.emit(true);
    } else {
      this.disableNext.emit(false);
    }
  }

  ngAfterViewInit(): void {
  }


}
