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
import {MatPaginator, MatTableDataSource} from '@angular/material';
import {NodeItem} from '../../../../../models/node_item.model';
import {NodeWizardForm} from '../../../../../models/forms/node_wizard_form.model';

@Component({
  selector: 'app-node-manual-add',
  templateUrl: './node-manual-add.component.html',
  styleUrls: ['./node-manual-add.component.scss']
})
export class NodeManualAddComponent implements OnInit {
  @Input() nodeWizardForm: NodeWizardForm;
  nodeItem: NodeItem;
  @ViewChild(MatPaginator) nodeListPaginator: MatPaginator;
  nodeListDataSource = new MatTableDataSource();
  nodeListDisplayedColumns = ['ipAddress', 'port'];
  @Output() disableNext = new EventEmitter();

  constructor() {
    this.nodeItem = new NodeItem();
    this.nodeWizardForm.multipleNodeAdd = false;
  }

  ngOnInit() {
    this.disableNext.emit(true);
    this.nodeWizardForm.singleNode = new NodeItem();

  }

  clearNode() {
    this.nodeWizardForm.singleNode = new NodeItem();
    this.disableNext.emit(true);
  }

  clearList() {
    this.nodeWizardForm.multipleNodes = [];
    this.nodeListDataSource = new MatTableDataSource<any>(this.nodeWizardForm.multipleNodes);
    this.nodeListDataSource.paginator = this.nodeListPaginator;
    this.disableNext.emit(true);
  }

  addNodeToList() {
    this.nodeWizardForm.multipleNodes.push(this.nodeItem);
    this.clearNode();
    this.nodeListDataSource = new MatTableDataSource<any>(this.nodeWizardForm.multipleNodes);
    this.nodeListDataSource.paginator = this.nodeListPaginator;
  }
}


