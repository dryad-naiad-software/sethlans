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
import {SetupForm} from '../../../../models/setupForm.model';
import {ComputeMethod} from '../../../../enums/compute.method.enum';
import {GPU} from '../../../../models/gpu.model';
import {HttpClient} from '@angular/common/http';
import {Node} from '../../../../models/node.model';


@Component({
  selector: 'app-node-config',
  templateUrl: './node-config.component.html',
  styleUrls: ['./node-config.component.scss']
})
export class NodeConfigComponent implements OnInit {
  @Input() setupForm: SetupForm;
  totalCores: number;
  availableComputeMethods: ComputeMethod[];
  availableGPUs: GPU[];


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    if (this.setupForm.node == null) {
      this.setupForm.node = new Node();
      this.setupForm.node.computeMethod = ComputeMethod.CPU;
    }
    this.http.get('/api/info/total_cores', {responseType: 'text'})
      .subscribe((cores: any) => {
        this.totalCores = cores;
        if (this.setupForm.node.cores == null) {
          this.setupForm.node.cores = cores;
        }
      });

    this.http.get('/api/info/available_methods')
      .subscribe(
        (computeMethods: any[]) => {
          this.availableComputeMethods = computeMethods;
          if (this.availableComputeMethods.length > 1) {
            this.setupForm.node.gpuEmpty = true;
            this.setupForm.node.selectedGPUDeviceIDs = [];
            this.http.get('/api/info/available_gpus')
              .subscribe((gpus: any[]) => {
                this.availableGPUs = gpus;
              });
          }
        });

  }

  methodSelection() {
    if (this.setupForm.node.computeMethod !== ComputeMethod.CPU) {
      this.setupForm.node.cores = this.totalCores;
      if (this.setupForm.node.selectedGPUDeviceIDs.length == 0) {
        this.setupForm.node.gpuEmpty = true;
      } else {
        this.setupForm.node.gpuEmpty = false;
      }
    }
    if (this.setupForm.node.computeMethod === ComputeMethod.CPU) {
      // gpuEmpty is used to control the toggling of the Save button. False means that the node settings can be saved.
      // CPU mode this is always set to false.
      this.setupForm.node.cores = this.totalCores;
      this.setupForm.node.selectedGPUDeviceIDs = [];
      this.setupForm.node.combined = true;
      this.setupForm.node.gpuEmpty = false;

    }
    if (this.setupForm.node.computeMethod === ComputeMethod.GPU) {
      this.setupForm.node.cores = null;
    }
  }

}
