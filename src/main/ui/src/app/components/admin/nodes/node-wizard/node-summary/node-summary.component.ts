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

import {Component, Input, OnInit} from '@angular/core';
import {NodeWizardForm} from '../../../../../models/forms/node_wizard_form.model';
import {NodeInfo} from '../../../../../models/node_info.model';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-node-summary',
  templateUrl: './node-summary.component.html',
  styleUrls: ['./node-summary.component.scss']
})
export class NodeSummaryComponent implements OnInit {
  @Input() nodeWizardForm: NodeWizardForm;
  @Input() accessKey: string;
  nodeToAdd: NodeInfo;
  nodesToAdd: NodeInfo[] = [];
  keyPresent: boolean;
  downloadComplete: boolean;


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    if (this.nodeWizardForm.multipleNodeAdd) {
      this.multiNodeQuery();
    } else {
      this.singleNodeQuery();
    }
  }

  singleNodeQuery() {
    this.nodeWizardForm.summaryComplete = false;
    this.downloadComplete = false;
    this.http.get('/api/management/is_key_present?ip=' + this.nodeWizardForm.singleNode.ipAddress + '&port=' + this.nodeWizardForm.singleNode.port).subscribe((value: boolean) => {
      this.keyPresent = value;
      console.log(value);
      if (value) {
        this.http.get('/api/management/node_check?ip=' + this.nodeWizardForm.singleNode.ipAddress + '&port=' + this.nodeWizardForm.singleNode.port).subscribe((node: NodeInfo) => {
          if (node != null) {
            this.nodeToAdd = node;
            this.nodeWizardForm.summaryComplete = true;
            this.downloadComplete = true;
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
    this.nodeWizardForm.summaryComplete = false;
    this.downloadComplete = false;
    this.nodeWizardForm.multipleNodes.forEach((value, idx, array) => {
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
              this.nodeWizardForm.summaryComplete = true;
            }
          });
        } else {
          value.active = false;
          if (idx === array.length - 1) {
            this.nodeWizardForm.summaryComplete = true;
          }
        }
      });
    });
  }

}
