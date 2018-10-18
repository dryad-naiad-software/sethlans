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
import {ComputeMethod} from '../../../enums/compute.method.enum';
import {GPU} from '../../../models/gpu.model';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {SethlansNode} from '../../../models/sethlan_node.model';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-compute-settings',
  templateUrl: './compute-settings.component.html',
  styleUrls: ['./compute-settings.component.scss']
})
export class ComputeSettingsComponent implements OnInit {
  computeMethods: any = ComputeMethod;
  editSettings: boolean = false;
  availableComputeMethods: ComputeMethod[] = [];
  totalCores: number;
  availableCores: number;
  availableGPUs: GPU[] = [];
  currentNode: SethlansNode = new SethlansNode();
  changedNode: SethlansNode = new SethlansNode();
  selectedGPUNames: string[];
  disableNext: boolean;
  method: any = ComputeMethod;


  constructor(private http: HttpClient, private modalService: NgbModal) {
  }

  switchToEdit() {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.editSettings = true;
  }

  switchToNormal() {
    document.body.style.background = '#EAEAEA';
    this.editSettings = false;
    this.changedNode = Object.assign({}, this.currentNode);
  }

  ngOnInit() {
    // Pre-pop max and available values
    this.prePopValues();
    // Populate Current Node
    this.retrieveCurrentSettings();
  }

  updateAndRestart(content) {
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/setup/update_compute', JSON.stringify(this.changedNode), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        setTimeout(() => {
          window.location.href = '/';
        }, 30000);
      }
    });
    this.modalService.open(content, options);
  }

  methodSelection() {
    if (this.changedNode.computeMethod !== ComputeMethod.CPU) {
      this.availableCores = this.totalCores - 1;
      this.changedNode.cores = this.totalCores;
      this.changedNode.gpuEmpty = this.changedNode.selectedGPUDeviceIDs.length == 0;
      if (this.changedNode.gpuEmpty) {
        this.disableNext = true;
      }
    }
    if (this.changedNode.computeMethod === ComputeMethod.CPU) {
      // gpuEmpty is used to control the toggling of the Save button. False means that the node settings can be saved.
      // CPU mode this is always set to false.
      this.availableCores = this.totalCores;
      this.changedNode.cores = this.availableCores;
      this.changedNode.selectedGPUDeviceIDs = [];
      this.changedNode.combined = true;
      this.changedNode.gpuEmpty = false;
      this.disableNext = false;
    }
    if (this.changedNode.computeMethod === ComputeMethod.GPU) {
      this.changedNode.cores = 1;
    }
  }

  retrieveCurrentSettings() {
    this.selectedGPUNames = [];
    this.http.get('/api/management/current_node')
      .subscribe((currentNode: SethlansNode) => {
        this.currentNode = Object.assign({}, currentNode);
        this.changedNode = Object.assign({}, currentNode);
        this.changedNode.totalCores = this.totalCores;
        this.currentNode.totalCores = this.totalCores;
        if (currentNode.computeMethod != ComputeMethod.CPU) {
          this.availableCores = this.totalCores - 1;
          this.http.get('/api/management/selected_gpus')
            .subscribe((selectedGPUs: GPU[]) => {
              for (let gpu of selectedGPUs) {
                this.selectedGPUNames.push(gpu.model);
              }
            });
        }
      });
  }


  prePopValues() {
    this.http.get('/api/info/available_methods')
      .subscribe(
        (computeMethods: any[]) => {
          this.availableComputeMethods = computeMethods;
        }, (error) => console.log(error));
    this.http.get('/api/info/total_cores', {responseType: 'text'})
      .subscribe((cores: any) => {
        this.totalCores = cores;
        this.availableCores = cores;
      }, (error) => console.log(error));
    if (this.availableComputeMethods.indexOf(ComputeMethod.GPU)) {
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
        });
    }
  }

  selected(event, string) {
    let checked = event.currentTarget.checked;
    if (checked) {
      let currentNode = this.changedNode;
      this.availableGPUs.forEach(function (value) {
        if (value.deviceID == string) {
          currentNode.selectedGPUDeviceIDs.push(value.deviceID);
        }
      });
      this.changedNode.gpuEmpty = false;
      this.disableNext = false;

    } else if (!checked) {
      let selectedGPUDeviceIDs = this.changedNode.selectedGPUDeviceIDs;
      for (let i = 0; i < selectedGPUDeviceIDs.length; i++) {
        if (selectedGPUDeviceIDs[i] == string) {
          this.changedNode.selectedGPUDeviceIDs.splice(i, 1);
        }
      }
    }

    if (this.changedNode.selectedGPUDeviceIDs.length === 0) {
      this.changedNode.gpuEmpty = true;
      this.disableNext = true;
    }
  }


}
