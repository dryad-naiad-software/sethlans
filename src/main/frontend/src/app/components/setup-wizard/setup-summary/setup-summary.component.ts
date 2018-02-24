import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Mode} from "../../../enums/mode.enum";
import {ComputeMethod} from "../../../enums/compute.method.enum";

@Component({
  selector: 'app-setup-summary',
  templateUrl: './setup-summary.component.html',
  styleUrls: ['./setup-summary.component.scss']
})
export class SetupSummaryComponent implements OnInit {
  @Input() setupFormData;
  mode: any = Mode;
  computeMethodEnum: any = ComputeMethod;

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

  finish() {
    console.log("finished");
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post("/api/setup/submit", JSON.stringify(this.setupFormData), httpOptions).subscribe((submitted: boolean) => {
      console.log(submitted);
    });
  }

  previousStep() {
    this.setupFormData.setProgress(5);
  }
}
