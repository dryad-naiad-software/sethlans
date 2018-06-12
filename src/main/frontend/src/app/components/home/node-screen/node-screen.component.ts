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
import {HttpClient} from "@angular/common/http";
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {GPU} from "../../../models/gpu.model";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-node-screen',
  templateUrl: './node-screen.component.html',
  styleUrls: ['./node-screen.component.scss']
})
export class NodeScreenComponent implements OnInit {
  computeType: ComputeMethod;
  cpuName: string;
  totalMemory: string;
  selectedCores: string;
  freeSpace: number;
  totalSpace: number;
  usedSpace: number;
  selectedGPUModels: string[];
  availableGPUs: GPU[];
  totalSlots: number;
  gpuCombined: boolean;


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.getInfo();
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getInfo()
    });
  }

  getInfo() {
    this.http.get('/api/info/compute_type').subscribe((computeType: ComputeMethod) => {
      this.computeType = computeType;
    });

    this.http.get('/api/info/total_memory', {responseType: 'text'}).subscribe((memory: string) => {
      this.totalMemory = memory;
    });

    this.http.get('/api/info/is_gpu_combined').subscribe((isCombined: any) => {
      this.gpuCombined = isCombined;
    });

    this.http.get('/api/info/cpu_name', {responseType: 'text'}).subscribe((cpuName: string) => {
      this.cpuName = cpuName;
    });

    this.http.get('/api/info/selected_cores', {responseType: 'text'}).subscribe((cores: string) => {
      this.selectedCores = cores;
    });

    this.http.get('/api/info/node_total_slots').subscribe((slots: number) => {
      this.totalSlots = slots;
    });

    this.http.get('/api/info/client_free_space').subscribe((freespace: number) => {
      this.freeSpace = freespace;
    });

    this.http.get('/api/info/client_total_space').subscribe((totalspace: number) => {
      this.totalSpace = totalspace;
    });

    this.http.get('/api/info/client_used_space').subscribe((usedspace: number) => {
      this.usedSpace = usedspace;
    });

    this.http.get('/api/info/client_selected_gpu_models').subscribe((selectedGPUs: string[]) => {
      this.selectedGPUModels = selectedGPUs;
    });
    this.http.get('/api/info/available_gpus')
      .subscribe((gpus: any[]) => {
        this.availableGPUs = gpus;
      });

  }

}
