import {Component, Inject, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient} from "@angular/common/http";
import {WindowRef} from "../../../services/windowref.service";
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'app-setup-finished',
  templateUrl: './setup-finished.component.html',
  styleUrls: ['./setup-finished.component.scss']
})
export class SetupFinishedComponent implements OnInit {
  @Input() setupFormData;
  sethlansURL: string;

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient, private winRef: WindowRef, @Inject(DOCUMENT) private document: any) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
    this.sethlansURL = 'https://' + this.winRef.nativeWindow.location.hostname + ':' + this.setupFormData.getSethlansPort().toString() + '/';
    setTimeout(() => {
        this.document.location.href = this.sethlansURL;
      }
      , 15000);
  }

}
