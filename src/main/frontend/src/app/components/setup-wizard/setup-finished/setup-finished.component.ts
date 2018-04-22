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
      , 60000);
  }

}
