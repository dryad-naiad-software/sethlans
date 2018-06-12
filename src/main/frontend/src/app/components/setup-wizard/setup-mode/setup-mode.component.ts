/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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
import {Mode} from '../../../enums/mode.enum';
import {SetupFormDataService} from '../../../services/setupformdata.service';
import {Server} from '../../../models/server.model';

@Component({
  selector: 'app-setup-mode',
  templateUrl: './setup-mode.component.html',
  styleUrls: ['./setup-mode.component.scss']
})
export class SetupModeComponent implements OnInit {
  @Input() setupFormData;
  mode: any = Mode;
  selectedMode: Mode;


  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.selectedMode = this.setupFormData.getMode();
  }

  save() {
    this.setupFormDataService.setSethlansMode(this.selectedMode);
    if (this.selectedMode === Mode.SERVER) {
      let server: Server = new Server();
      this.setupFormData.setServer(server);
    }
    this.nextScreen();
  }

  nextScreen() {
    let currentProgress = this.setupFormData.getProgress();
    this.setupFormData.setProgress(currentProgress + 1);

  }

}
