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
import {NodeItem} from '../../../../models/node_item.model';
import {MatPaginator, MatTableDataSource} from '@angular/material';
import {GetStartedWizardForm} from '../../../../models/forms/get_started_wizard_form.model';

@Component({
  selector: 'app-wizard-node-auth',
  templateUrl: './wizard-node-auth.component.html',
  styleUrls: ['./wizard-node-auth.component.scss']
})
export class WizardNodeAuthComponent implements OnInit {
  @Input() getStartedWizardForm: GetStartedWizardForm;
  nodeItem: NodeItem;
  addDisabled: boolean;
  @ViewChild(MatPaginator) nodeListPaginator: MatPaginator;
  nodeListDataSource = new MatTableDataSource();
  @Output() disableNext = new EventEmitter();
  nodeListDisplayedColumns = ['ipAddress', 'port', 'action'];
  showPass: boolean;


  constructor() {
    this.nodeItem = new NodeItem();
    this.addDisabled = true;
    this.showPass = false;

  }

  ngOnInit() {
    this.refreshList();
  }

  addNodeToList() {
    this.getStartedWizardForm.listOfNodes.push(this.nodeItem);
    this.nodeItem = new NodeItem();
    this.refreshList();
    this.enableNext();
  }

  refreshList() {
    this.nodeListDataSource = new MatTableDataSource<any>(this.getStartedWizardForm.listOfNodes);
    this.nodeListDataSource.paginator = this.nodeListPaginator;
  }

  enableAdd() {
    this.addDisabled = this.nodeItem.nodeItemNotReady();
  }

  enableNext() {
    this.disableNext.emit(!(this.getStartedWizardForm.listOfNodes.length > 0 && !this.getStartedWizardForm.nodeLogin.loginNotReady()));
  }

  deleteNodeFromList(node: NodeItem) {
    let index = this.getStartedWizardForm.listOfNodes.indexOf(node);
    if (index > -1) {
      this.getStartedWizardForm.listOfNodes.splice(index, 1);
      this.refreshList();
    }
  }
}
