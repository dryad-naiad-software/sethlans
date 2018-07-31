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
import {HttpClient} from '@angular/common/http';
import {NodeItem} from '../../../../models/node_item.model';

@Component({
  selector: 'app-node-wizard',
  templateUrl: './node-wizard.component.html',
  styleUrls: ['./node-wizard.component.scss']
})
export class NodeWizardComponent implements OnInit {
  currentMode: NodeWizardMode;
  wizardModes: any = NodeWizardMode;
  wizardTypes: any = NodeAddType;
  addType: NodeAddType;
  accessKey: string;
  summaryComplete: boolean;
  finished: boolean = false;
  multipleNodes: NodeItem[];
  singleNode: NodeItem;
  nextDisabled: boolean;
  multipleNodeAdd: boolean;


  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.currentMode = NodeWizardMode.Start;
    this.nextDisabled = true;
    this.summaryComplete = false;

  }

  ngOnInit() {
    this.http.get('/api/management/get_key_from_server', {responseType: 'text'}).subscribe((key: string) => this.accessKey = key);
  }

  disableNext(value: boolean) {
    this.nextDisabled = value;
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

  onSelection() {
    if (this.addType != null) {
      this.nextDisabled = false;
    }
  }

  returnToNodes() {
    window.location.href = '/admin/nodes';
  }

  next() {
    switch (this.currentMode) {
      case NodeWizardMode.Start:
        this.currentMode = NodeWizardMode.Add;
        break;
    }
  }

  previous() {
    switch (this.currentMode) {
      case NodeWizardMode.Add:
        this.currentMode = NodeWizardMode.Start;
        break;
      case NodeWizardMode.Summary:
        this.currentMode = NodeWizardMode.Add;
        break;

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
