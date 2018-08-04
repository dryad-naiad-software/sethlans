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
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Node} from '../../../models/node.model';

@Component({
  selector: 'app-compute-settings',
  templateUrl: './compute-settings.component.html',
  styleUrls: ['./compute-settings.component.scss']
})
export class ComputeSettingsComponent implements OnInit {
  computeMethods: any = ComputeMethod;
  availableComputeMethods: ComputeMethod[] = [];
  totalCores: number;
  availableGPUs: GPU[] = [];
  currentNode: Node = new Node();
  gpuCombined: boolean;

  constructor(private http: HttpClient, private router: Router, private modalService: NgbModal) {
  }

  ngOnInit() {
    // Pre-pop max and available values
    this.prePopValues();
    // Populate Current Node
    this.setCurrentNode();
  }

  setCurrentNode() {
    this.http.get('/api/management/selected_compute_method')
      .subscribe((selectedMethod: ComputeMethod) => {
        this.currentNode.computeMethod = selectedMethod;
      });
    this.http.get('/api/management/selected_gpus')
      .subscribe((selectedGPUs: GPU[]) => {
        this.currentNode.selectedGPUDeviceIDs = [];
        for (let gpu of selectedGPUs) {
          this.currentNode.selectedGPUDeviceIDs.push(gpu.deviceID);
        }
      });
    this.http.get('/api/info/is_gpu_combined').subscribe((isCombined: any) => {
      this.gpuCombined = isCombined;
    });
    this.http.get('/api/management/current_cores')
      .subscribe((currentCores: any) => {
        if (currentCores == 0) {
          this.currentNode.cores = this.totalCores;
        } else {
          this.currentNode.cores = currentCores;
        }
      });
    this.http.get('/api/management/current_tilesize_cpu')
      .subscribe((tileSizeCPU: any) => {
        this.currentNode.tileSizeCPU = tileSizeCPU;
      });
    this.http.get('/api/management/current_tilesize_gpu')
      .subscribe((tileSizeGPU: any) => {
        this.currentNode.tileSizeGPU = tileSizeGPU;
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
      }, (error) => console.log(error));
    if (this.availableComputeMethods.indexOf(ComputeMethod.GPU)) {
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
          this.http.get('/api/management/selected_gpus')
            .subscribe((selectedGPUs: GPU[]) => {
              for (let selectedGPU of selectedGPUs) {
                for (let availGPU of this.availableGPUs) {
                  if (selectedGPU.model == availGPU.model) {
                    availGPU.selected = true;
                  }
                }
              }

            });
        }, (error) => console.log(error));
    }
  }

}
