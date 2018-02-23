import {Component, Input, OnInit} from '@angular/core';
import {Node} from "../../../models/node.model";
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";
import {ComputeMethod} from "../../../enums/compute.method";
import {GPU} from "../../../models/gpu.model";
import {Mode} from "../../../enums/mode";


@Component({
  selector: 'app-setup-node',
  templateUrl: './setup-node.component.html',
  styleUrls: ['./setup-node.component.scss']
})
export class SetupNodeComponent implements OnInit {
  @Input() setupFormData;
  node: Node = new Node();
  mode: any = Mode;
  computeMethodEnum: any = ComputeMethod;
  availableComputeMethods: ComputeMethod[] = [];
  totalCores: number;
  availableGPUs: GPU[] = [];

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.setupFormData.setNode(this.node);
    this.http.get('/api/info/available_methods')
      .subscribe(
        (computeMethods: any[]) => {
          this.availableComputeMethods = computeMethods;
          console.log(this.availableComputeMethods);
          this.node.computeMethod = ComputeMethod.CPU;
        }, (error) => console.log(error));
    this.http.get('/api/info/total_cores', {responseType: 'text'})
      .subscribe((cores: any) => {
        this.totalCores = cores;
        this.node.cores = cores;
        console.log(this.totalCores);
      }, (error) => console.log(error));
    if (this.availableComputeMethods.indexOf(ComputeMethod.GPU)) {
      this.node.selectedGPUs = [];
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
          console.log(this.availableGPUs);
        }, (error) => console.log(error));
    }
  }

  selected(event, gpu) {
    let checked = event.currentTarget.checked;
    console.log(event.currentTarget.checked);
    console.log(gpu);
    if (checked) {
      this.node.selectedGPUs.push(gpu);
      this.node.gpuEmpty = false;
    } else if (!checked) {
      const index = this.node.selectedGPUs.indexOf(gpu);
      this.node.selectedGPUs.splice(index, 1);
      this.node.gpuEmpty = true;
    }
    console.log(this.node.selectedGPUs);
  }

  methodSelection() {
    if (this.node.computeMethod !== ComputeMethod.CPU) {
      if (this.node.selectedGPUs.length == 0) {
        this.node.gpuEmpty = true;
        console.log(this.node.gpuEmpty);
      } else {
        this.node.gpuEmpty = false;
        console.log(this.node.gpuEmpty);
      }
    }
    if (this.node.computeMethod === ComputeMethod.CPU) {
      // gpuEmpty is used to control the toggling of the Save button. False means that the node settings can be saved.
      // CPU mode this is always set to false.
      this.node.gpuEmpty = false;
    }
    if (this.node.computeMethod === ComputeMethod.GPU) {
      this.node.cores = null;
    }
  }

  save() {
    if (this.node.computeMethod === ComputeMethod.CPU) {
      this.node.selectedGPUs = null;
    }
    else if (this.node.computeMethod === ComputeMethod.GPU) {
      this.node.cores = null;
    }
    this.setupFormData.setNode(this.node);
    this.nextStep();
  }

  previousStep() {
    this.setupFormData.setNode(null);
    this.setupFormData.setProgress(1);
  }

  nextStep() {
    this.setupFormData.setProgress(5);
  }

}
