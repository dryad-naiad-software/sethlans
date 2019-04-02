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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NodeWizardForm} from '../../../../../models/forms/node_wizard_form.model';

@Component({
  selector: 'app-node-auth',
  templateUrl: './node-auth.component.html',
  styleUrls: ['./node-auth.component.scss']
})
export class NodeAuthComponent implements OnInit {
  @Input() nodeWizardForm: NodeWizardForm;
  @Output() disableNext = new EventEmitter();
  @Output() clickNext = new EventEmitter();
  showPass: boolean;


  constructor() {
    this.showPass = false;
  }

  ngOnInit() {
    this.disableNext.emit(true);
  }


  checkAuthOption(event) {
    if (this.nodeWizardForm.dontUseAuth) {
      this.nodeWizardForm.authOptionSelected = true;
      this.disableNext.emit(false);
    } else if (!this.nodeWizardForm.dontUseAuth && !this.nodeWizardForm.nodeLogin.loginNotReady()) {
      this.nodeWizardForm.authOptionSelected = true;
      this.disableNext.emit(false);
      if (event.key === 'Enter') {
        this.clickNext.emit();
      }
    } else {
      this.nodeWizardForm.authOptionSelected = false;
      this.disableNext.emit(true);
    }
  }

}
