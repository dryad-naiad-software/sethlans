/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {SetupForm} from "../models/forms/setup-form.model";
import {Observable} from "rxjs";
import {catchError} from "rxjs/operators";
import {HandleError, HttpErrorHandler} from './http-error-handler.service';


@Injectable({
  providedIn: 'root'
})

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class SethlansService {
  private handleError: HandleError;


  rootURL = '/api/v1';
  firstTime: boolean = false;

  constructor(private http: HttpClient, httpErrorHandler: HttpErrorHandler) {
    this.handleError = httpErrorHandler.createHandleError('SethlansService');

  }

  isFirstTime() {
    return this.http.get(this.rootURL + '/info/is_first_time');
  }

  version() {
    return this.http.get(this.rootURL + '/info/version');
  }

  mode() {
    return this.http.get(this.rootURL + "/info/mode")
  }

  year() {
    return this.http.get(this.rootURL + "/info/build_year")
  }

  getSetup() {
    return this.http.get(this.rootURL + "/setup/get_setup")
  }

  getNodeDashBoard() {
    return this.http.get(this.rootURL + "/info/node_dashboard")
  }

  getCurrentUser() {
    return this.http.get(this.rootURL + "/management/get_current_user")
  }

  isAuthenticated() {
    return this.http.get(this.rootURL + "/management/is_authenticated")
  }

  getServersOnNode() {
    return this.http.get(this.rootURL + "/management/authorized_server_on_node")
  }

  getNodeAPIKey() {
    return this.http.get(this.rootURL + "/management/node_api_key")
  }

  submitSetup(setupForm: SetupForm | undefined): Observable<any> {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    return this.http.post<SetupForm>(this.rootURL + "/setup/submit", JSON.stringify(setupForm),
      {headers: headers, observe: "response"})
      .pipe(
        catchError(this.handleError('submitSetup', setupForm)))

  }

  setupRestart() {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.get(this.rootURL + "/setup/restart", {headers: headers, observe: "response"})
  }

}
