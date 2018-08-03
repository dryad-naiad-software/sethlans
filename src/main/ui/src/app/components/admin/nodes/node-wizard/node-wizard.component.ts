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
import {NodeWizardMode} from '../../../../enums/node_wizard_mode.enum';
import {NodeAddType} from '../../../../enums/node_wizard_add_type.enum';
import {NodeWizardForm} from '../../../../models/forms/node_wizard_form.model';

@Component({
  selector: 'app-node-wizard',
  templateUrl: './node-wizard.component.html',
  styleUrls: ['./node-wizard.component.scss']
})
export class NodeWizardComponent implements OnInit {
  wizardModes: any = NodeWizardMode;
  wizardTypes: any = NodeAddType;
  accessKey: string;
  nodeWizardForm: NodeWizardForm;
  nextDisabled: boolean;


  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.nodeWizardForm = new NodeWizardForm();
    this.nextDisabled = true;

  }

  ngOnInit() {
    this.http.get('/api/management/get_key_from_server', {responseType: 'text'}).subscribe((key: string) => this.accessKey = key);
  }

  disableNext(value: boolean) {
    this.nextDisabled = value;
  }

  copyKey() {
    const selBox = document.createElement('textarea');
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
    if (this.nodeWizardForm.addType != null) {
      this.nextDisabled = false;
    }
  }

  returnToNodes() {
    window.location.href = '/admin/nodes';
  }

  next() {
    switch (this.nodeWizardForm.currentMode) {
      case NodeWizardMode.Start:
        this.nodeWizardForm.currentMode = NodeWizardMode.Add;
        break;
      case NodeWizardMode.Add:
        this.nodeWizardForm.currentMode = NodeWizardMode.Summary;
        break;
    }
  }

  previous() {
    switch (this.nodeWizardForm.currentMode) {
      case NodeWizardMode.Add:
        this.nodeWizardForm.currentMode = NodeWizardMode.Start;
        break;
      case NodeWizardMode.Summary:
        this.nodeWizardForm.currentMode = NodeWizardMode.Add;
        break;

    }
  }

}




