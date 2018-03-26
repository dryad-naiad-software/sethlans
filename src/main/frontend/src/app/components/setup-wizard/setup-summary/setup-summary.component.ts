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

import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../../../services/setupformdata.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Mode} from "../../../enums/mode.enum";
import {SetupComputeMethod} from "../../../enums/setup_compute.method.enum";

@Component({
  selector: 'app-setup-summary',
  templateUrl: './setup-summary.component.html',
  styleUrls: ['./setup-summary.component.scss']
})
export class SetupSummaryComponent implements OnInit {
  @Input() setupFormData;
  mode: any = Mode;
  computeMethodEnum: any = SetupComputeMethod;
  finished = false;

  constructor(private setupFormDataService: SetupFormDataService, private http: HttpClient) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

  finish() {
    console.log("finished");
    this.finished = true;
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post("/api/setup/submit", JSON.stringify(this.setupFormData), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        this.setupFormData.setProgress(7);
      }
    });
  }

  previousStep() {
    this.setupFormData.setProgress(5);
  }
}
