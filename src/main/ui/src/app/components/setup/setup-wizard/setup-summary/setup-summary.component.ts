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
import {SetupWizardForm} from '../../../../models/forms/setup_wizard_form.model';
import {Mode} from '../../../../enums/mode.enum';
import {GPU} from '../../../../models/gpu.model';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-setup-summary',
  templateUrl: './setup-summary.component.html',
  styleUrls: ['./setup-summary.component.scss']
})
export class SetupSummaryComponent implements OnInit {
  @Input() setupForm: SetupWizardForm;
  mode: any = Mode;
  availableGPUs: GPU[];
  selectedGPUNames: string[];


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    if (this.setupForm.node != null && !this.setupForm.node.gpuEmpty) {
      this.selectedGPUNames = [];
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
          let gpuList = this.availableGPUs;
          let gpuNames = this.selectedGPUNames;
          this.setupForm.node.selectedGPUDeviceIDs.forEach(function (deviceID) {
            let id;
            for (id in gpuList) {
              if (deviceID === gpuList[id].deviceID) {
                gpuNames.push(gpuList[id].model);
              }
            }
          });
        });

    }
  }

}
