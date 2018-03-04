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
  selectedGPUIds: GPU[];

  constructor(private http: HttpClient) {
  }

  ngOnInit() {

  }

}
