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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SetupWizardForm} from '../../../../models/forms/setup_wizard_form.model';

@Component({
  selector: 'app-dual-config',
  templateUrl: './dual-config.component.html',
  styleUrls: ['./dual-config.component.scss']
})
export class DualConfigComponent implements OnInit {
  @Input() setupForm: SetupWizardForm;
  @Output() disableNext = new EventEmitter();


  constructor() {
  }

  ngOnInit() {
  }

  passDisable(value: boolean) {
    this.disableNext.emit(value);
  }
}
