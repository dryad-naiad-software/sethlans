import {Component, Input, OnInit} from '@angular/core';
import {Node} from "../../../models/node.model";
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";
import {ComputeMethod} from "../../../enums/compute.method.enum";
import {GPU} from "../../../models/gpu.model";
import {Mode} from "../../../enums/mode.enum";


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
          this.node.setComputeMethod(ComputeMethod.CPU);
        }, (error) => console.log(error));
    this.http.get('/api/info/total_cores', {responseType: 'text'})
      .subscribe((cores: any) => {
        this.totalCores = cores;
        this.node.setCores(cores);
        console.log(this.totalCores);
      }, (error) => console.log(error));
    if (this.availableComputeMethods.indexOf(ComputeMethod.GPU)) {
      this.node.setSelectedGPUs([]);
      this.http.get('/api/info/available_gpus')
        .subscribe((gpus: any[]) => {
          this.availableGPUs = gpus;
          console.log(this.availableGPUs);
        }, (error) => console.log(error));
    }
  }

  selected(event, gpu: GPU) {
    let checked = event.currentTarget.checked;
    console.log(event.currentTarget.checked);
    console.log(gpu);
    if (checked) {
      gpu.selected = true;
      this.node.getSelectedGPUs().push(gpu);
      this.node.setGpuEmpty(false);
    } else if (!checked) {
      let selectedGPUs = this.node.getSelectedGPUs();
      for (let i = 0; i < selectedGPUs.length; i++) {
        if (selectedGPUs[i].deviceID == gpu.deviceID) {
          this.node.getSelectedGPUs().splice(i, 1);
        }
      }
    }
    if (this.node.getSelectedGPUs().length === 0) {
      this.node.setGpuEmpty(true);
    }
  }

  methodSelection() {
    if (this.node.getComputeMethod() !== ComputeMethod.CPU) {
      if (this.node.getSelectedGPUs().length == 0) {
        this.node.setGpuEmpty(true);
        console.log(this.node.isGpuEmpty());
      } else {
        this.node.setGpuEmpty(false);
        console.log(this.node.isGpuEmpty());
      }
    }
    if (this.node.getComputeMethod() === ComputeMethod.CPU) {
      // gpuEmpty is used to control the toggling of the Save button. False means that the node settings can be saved.
      // CPU mode this is always set to false.
      this.node.setGpuEmpty(false);
    }
    if (this.node.getComputeMethod() === ComputeMethod.GPU) {
      this.node.setCores(null);
    }
  }

  save() {
    if (this.node.getComputeMethod() === ComputeMethod.CPU) {
      this.node.setSelectedGPUs(null);
    }
    else if (this.node.getComputeMethod() === ComputeMethod.GPU) {
      this.node.setCores(null);
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
