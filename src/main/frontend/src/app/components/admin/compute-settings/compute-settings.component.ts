import {Component, OnInit} from '@angular/core';
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {GPU} from "../../../models/gpu.model";
import {HttpClient} from "@angular/common/http";
import {Node} from "../../../models/node.model";

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
  node: Node = new Node();
  currentCores: number;
  selectedGPUIds: GPU[];

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/info/available_methods')
      .subscribe(
        (computeMethods: any[]) => {
          this.availableComputeMethods = computeMethods;
          console.log(this.availableComputeMethods);
        }, (error) => console.log(error));
    this.http.get('/api/info/total_cores', {responseType: 'text'})
      .subscribe((cores: any) => {
        this.totalCores = cores;
        this.node.setCores(cores);
        console.log(this.totalCores);
      }, (error) => console.log(error));
    this.http.get('/api/info/current_cores', {responseType: 'text'}).subscribe((currentCore: any) => {
      this.currentCores = currentCore;
      this.node.setCores(currentCore);
    });
    this.http.get('/api/info/selected_compute_method', {responseType: 'text'}).subscribe((selectedComputeMethod: any) => {
      this.node.setComputeMethod(selectedComputeMethod);
    });
    if (this.availableComputeMethods.indexOf(ComputeMethod.GPU)) {
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
          console.log(this.availableGPUs);
        }, (error) => console.log(error));
      this.http.get('/api/info/selected_gpus')
        .subscribe((selectedgpus: any[]) => {
          this.selectedGPUIds = selectedgpus;
          this.node.setSelectedGPUs(selectedgpus);
          console.log(this.selectedGPUIds);
        }, (error) => console.log(error));
    }
  }

}
