import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-sethlans-settings',
  templateUrl: './sethlans-settings.component.html',
  styleUrls: ['./sethlans-settings.component.scss']
})
export class SethlansSettingsComponent implements OnInit {

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

}
