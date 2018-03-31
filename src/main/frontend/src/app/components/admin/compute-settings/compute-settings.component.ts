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

import {Component, OnInit} from '@angular/core';
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {GPU} from "../../../models/gpu.model";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Node} from "../../../models/node.model";
import {Router} from "@angular/router";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-compute-settings',
  templateUrl: './compute-settings.component.html',
  styleUrls: ['./compute-settings.component.scss']
})
export class ComputeSettingsComponent implements OnInit {
  computeMethodEnum: any = ComputeMethod;
  availableComputeMethods: ComputeMethod[] = [];
  totalCores: number;
  availableGPUs: GPU[] = [];
  currentNode: Node = new Node();
  newNode: Node = new Node();

  constructor(private http: HttpClient, private router: Router, private modalService: NgbModal) {
  }

  ngOnInit() {
    // Pre-pop max and available values
    this.prePopValues();
    // Populate Current Node
    this.setCurrentNode();
  }

  open(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }

  updateAndRestart(content) {
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post("/api/setup/update_compute", JSON.stringify(this.newNode), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        setTimeout(() => {
          this.router.navigateByUrl("/").then(() => {
            location.reload();
          });
        }, 15000);
      }
    });
    this.modalService.open(content, options);
  }

  setCurrentNode() {
    this.http.get('/api/management/selected_compute_method')
      .subscribe((selectedMethod: ComputeMethod) => {
        this.currentNode.setComputeMethod(selectedMethod);
        this.newNode.setComputeMethod(selectedMethod)
      });
    this.http.get('/api/management/selected_gpus')
      .subscribe((selectedGPUs: GPU[]) => {
        this.currentNode.selectedGPUs = [];
        for (let gpu of selectedGPUs) {
          this.currentNode.selectedGPUs.push(gpu);
        }
        this.newNode.setSelectedGPUs(selectedGPUs);
        for (let gpu of this.newNode.getSelectedGPUs()) {
          gpu.selected = true;
        }
        console.log(this.currentNode.getSelectedGPUs());
      });
    this.http.get('/api/management/current_cores')
      .subscribe((currentCores: any) => {
        this.currentNode.setCores(currentCores);
        this.newNode.setCores(currentCores);
      });
    this.http.get('/api/management/current_tilesize_cpu')
      .subscribe((tileSizeCPU: any) => {
        this.currentNode.setTileSizeCPU(tileSizeCPU);
        this.newNode.setTileSizeCPU(tileSizeCPU)
      });
    this.http.get('/api/management/current_tilesize_gpu')
      .subscribe((tileSizeGPU: any) => {
        this.currentNode.setTileSizeGPU(tileSizeGPU);
        this.newNode.setTileSizeGPU(tileSizeGPU);
      });
  }

  selected(event, gpu: GPU) {
    let checked = event.currentTarget.checked;
    console.log(event.currentTarget.checked);
    if (checked) {
      gpu.selected = true;
      this.newNode.getSelectedGPUs().push(gpu);
      this.newNode.setGpuEmpty(false);
    } else if (!checked) {
      let selectedGPUs = this.newNode.getSelectedGPUs();
      for (let i = 0; i < selectedGPUs.length; i++) {
        if (selectedGPUs[i].deviceID == gpu.deviceID) {
          this.newNode.getSelectedGPUs().splice(i, 1);
        }
      }
    }
    if (this.newNode.getSelectedGPUs().length === 0) {
      this.newNode.setGpuEmpty(true);
    }
  }

  cancel() {
    window.location.href = "/admin/compute_settings";
  }

  methodSelection() {
    if (this.newNode.getComputeMethod() !== ComputeMethod.CPU) {
      if (this.newNode.getSelectedGPUs().length == 0) {
        this.newNode.setGpuEmpty(true);
      } else {
        this.newNode.setGpuEmpty(false);
        console.log(this.newNode.isGpuEmpty());
      }
    }
    if (this.newNode.getComputeMethod() === ComputeMethod.CPU) {
      // gpuEmpty is used to control the toggling of the Save button. False means that the node settings can be saved.
      // CPU mode this is always set to false.
      this.newNode.setGpuEmpty(false);
      this.newNode.setTileSizeGPU(this.currentNode.tileSizeGPU);
      this.newNode.setSelectedGPUs([]);
      for (let gpu of this.currentNode.selectedGPUs) {
        this.newNode.selectedGPUs.push(gpu);
      }
    }
    if (this.newNode.getComputeMethod() === ComputeMethod.GPU) {
      this.newNode.setCores(this.currentNode.cores);
      this.newNode.setTileSizeCPU(this.currentNode.tileSizeCPU);
    }
  }

  prePopValues() {
    this.http.get('/api/info/available_methods')
      .subscribe(
        (computeMethods: any[]) => {
          this.availableComputeMethods = computeMethods;
          console.log(this.availableComputeMethods);
        }, (error) => console.log(error));
    this.http.get('/api/info/total_cores', {responseType: 'text'})
      .subscribe((cores: any) => {
        this.totalCores = cores;
        console.log(this.totalCores);
      }, (error) => console.log(error));
    if (this.availableComputeMethods.indexOf(ComputeMethod.GPU)) {
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
          console.log(this.availableGPUs);
          this.http.get('/api/management/selected_gpus')
            .subscribe((selectedGPUs: GPU[]) => {
              console.log("Test");
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
