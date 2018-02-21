import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-setup-summary',
  templateUrl: './setup-summary.component.html',
  styleUrls: ['./setup-summary.component.scss']
})
export class SetupSummaryComponent implements OnInit {
  @Input() setupFormData;

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

  finish() {

  }

  previousStep() {
    this.setupFormData.setProgress(5);
  }
}
