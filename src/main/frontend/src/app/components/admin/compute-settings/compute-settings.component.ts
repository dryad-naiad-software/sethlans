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
  currentNode: Node = new Node();
  selectedGPUs: GPU[];
  newNode: Node = new Node();

  constructor(private http: HttpClient) {
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
        this.currentNode.setComputeMethod(selectedMethod);
      });
    this.http.get('/api/management/selected_gpus')
      .subscribe((selectedGPUs: GPU[]) => {
        this.selectedGPUs = selectedGPUs;
        this.currentNode.setSelectedGPUs(this.selectedGPUs);
        console.log(this.selectedGPUs);
      });
    this.http.get('/api/management/current_cores')
      .subscribe((currentCores: any) => {
        this.currentNode.setCores(currentCores);
      });
    this.http.get('/api/management/current_tilesize_cpu')
      .subscribe((tileSizeCPU: any) => {
        this.currentNode.setTileSizeCPU(tileSizeCPU);
      });
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
        }, (error) => console.log(error));
    }
  }

}
