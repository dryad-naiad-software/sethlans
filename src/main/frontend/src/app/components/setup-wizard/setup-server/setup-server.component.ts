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
import {Server} from '../../../models/server.model';
import {SetupFormDataService} from '../../../services/setupformdata.service';
import {HttpClient} from '@angular/common/http';
import {Mode} from '../../../enums/mode.enum';

@Component({
  selector: 'app-setup-server',
  templateUrl: './setup-server.component.html',
  styleUrls: ['./setup-server.component.scss']
})
export class SetupServerComponent implements OnInit {
  @Input() setupFormData;
  server: Server = new Server();
  mode: any = Mode;
  availableBlenderVersions: string[] = [];

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.setupFormData.setServer(this.server);
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions) => {
          this.availableBlenderVersions = blenderVersions['blenderVersions'];
          this.server.setBlenderVersion(this.availableBlenderVersions[0]);
        }, (error) => console.log(error));
  }

  previousStep() {
    this.setupFormData.setServer(null);
    this.setupFormData.setProgress(1);
  }

  save() {
    this.setupFormData.setServer(this.server);
    this.nextStep();
  }

  nextStep() {
    this.setupFormData.setProgress(5);
  }
}
