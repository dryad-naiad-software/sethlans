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
import {SetupFormDataService} from '../../../services/setupformdata.service';
import {HttpClient} from '@angular/common/http';
import {Mode} from '../../../enums/mode.enum';

@Component({
  selector: 'app-setup-settings',
  templateUrl: './setup-settings.component.html',
  styleUrls: ['./setup-settings.component.scss']
})
export class SetupSettingsComponent implements OnInit {
  @Input() setupFormData;

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.http.get('/api/info/root_directory',)
      .subscribe((rootDirectory) => {
        this.setupFormData.setRootDirectory(rootDirectory['root_dir']);
      });
    this.http.get('/api/info/sethlans_ip')
      .subscribe((sethlansIP) => {
        this.setupFormData.setIPAddress(sethlansIP['ip']);
      });
    this.http.get('/api/info/sethlans_port')
      .subscribe((sethlansPort) => {
        this.setupFormData.setSethlansPort(sethlansPort['port']);
      });
  }

  previousStep() {
    if (this.setupFormData.getMode() === Mode.SERVER) {
      this.setupFormData.setProgress(2);
    }
    else if (this.setupFormData.getMode() === Mode.NODE) {
      this.setupFormData.setProgress(3);
    }
    else if (this.setupFormData.getMode() === Mode.DUAL) {
      this.setupFormData.setProgress(4);
    }
  }

  save() {
    this.nextStep();

  }

  private nextStep() {
    this.setupFormData.setProgress(6)

  }
}
