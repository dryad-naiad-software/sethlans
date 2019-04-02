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
  @Output() disableNext = new EventEmitter();
  @Output() clickNext = new EventEmitter();
  addDisabled: boolean;
  nodeListDisplayedColumns = ['ipAddress', 'port', 'action'];
  @ViewChild(MatPaginator) nodeListPaginator: MatPaginator;
  nodeListDataSource = new MatTableDataSource();
  nodeItem: NodeItem;


  constructor() {
    this.nodeItem = new NodeItem();
    this.addDisabled = true;
  }

  ngOnInit() {
    this.refreshList();
    this.nodeWizardForm.listOfNodes = [];
    this.nodeWizardForm.nodesToAdd = [];
  }

  addNodeToList() {
    this.nodeWizardForm.listOfNodes.push(this.nodeItem);
    this.nodeItem = new NodeItem();
    this.refreshList();
    this.enableNext();
  }

  refreshList() {
    this.nodeListDataSource = new MatTableDataSource<any>(this.nodeWizardForm.listOfNodes);
    this.nodeListDataSource.paginator = this.nodeListPaginator;
  }

  enableAdd() {
    this.addDisabled = this.nodeItem.nodeItemNotReady();
  }

  enableNext() {
    this.disableNext.emit(!(this.nodeWizardForm.listOfNodes.length > 0));
  }

  deleteNodeFromList(node: NodeItem) {
    let index = this.nodeWizardForm.listOfNodes.indexOf(node);
    if (index > -1) {
      this.nodeWizardForm.listOfNodes.splice(index, 1);
      this.refreshList();
    }
  }
}


