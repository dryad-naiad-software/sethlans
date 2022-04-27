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
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {SetupForm} from "../models/forms/setup-form.model";
import {Observable} from "rxjs";
import {catchError} from "rxjs/operators";
import {HandleError, HttpErrorHandler} from './http-error-handler.service';
import {NodeSettings} from "../models/settings/nodesettings.model";
import {NodeForm} from "../models/forms/node-form.model";
import {ProjectForm} from "../models/forms/project-form.model";


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

  getAuthorizedServer() {
    return this.http.get(this.rootURL + "/management/authorized_server_on_node")
  }

  networkNodeScan() {
    return this.http.get(this.rootURL + "/management/network_node_scan")
  }

  getCurrentNodeList() {
    return this.http.get(this.rootURL + "/management/current_node_list")
  }

  getNodeSettings() {
    return this.http.get(this.rootURL + "/management/get_node_settings")
  }

  setNodeSettings(nodeSettings: NodeSettings) {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.post<any>(this.rootURL
      + "/management/change_node_settings", JSON.stringify(nodeSettings),
      {
        headers: headers,
        observe: "response"
      })
      .pipe(catchError(this.handleError('setNodeSettings', nodeSettings)))
  }

  getProjects() {
    return this.http.get(this.rootURL + "/project/project_list")

  }

  getServerAPIKey() {
    return this.http.get(this.rootURL + "/management/server_api_key")
  }

  getNodeAPIKey() {
    return this.http.get(this.rootURL + "/management/node_api_key")
  }

  setNodeAPIKey(nodeAPIKey: string) {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    const params = new HttpParams()
      .append('api-key', nodeAPIKey)
    return this.http.post<any>(this.rootURL
      + "/management/set_node_api_key", '',
      {
        headers: headers,
        params: params,
        observe: "response"
      })
      .pipe(catchError(this.handleError('setNodeAPIKey', nodeAPIKey)))
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


  addNodesToServer(selectedNodes: Array<NodeForm>): Observable<any> {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.post<Array<NodeForm>>(this.rootURL + "/management/add_nodes_to_server", JSON.stringify(selectedNodes),
      {headers: headers, observe: "response"})
      .pipe(
        catchError(this.handleError('submitSetup', selectedNodes)))

  }

  setupRestart() {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.get(this.rootURL + "/setup/restart", {headers: headers, observe: "response"})
  }

  submitProject(projectForm: ProjectForm): Observable<any> {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    return this.http.post<ProjectForm>(this.rootURL + "/project/create_project",
      JSON.stringify(projectForm), {headers: headers, observe: "response"})
      .pipe(
        catchError(this.handleError('submitProject', projectForm)))
  }

  deleteProject(projectID: string): Observable<any> {
    return this.http.delete(this.rootURL + "/project/delete_project/" + projectID, {observe: 'response'})
      .pipe(
        catchError(this.handleError('deleteProject', projectID)))
  }

  startProject(projectID: string): Observable<any> {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    const params = new HttpParams()
      .append('projectID', projectID)
    return this.http.post(this.rootURL + "/project/start_project", '',
      {
        headers: headers,
        params: params,
        observe: "response"
      })
      .pipe(catchError(this.handleError('startProject', projectID)))
  }

  stopProject(projectID: string): Observable<any> {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });
    const params = new HttpParams()
      .append('projectID', projectID)
    return this.http.post(this.rootURL + "/project/stop_project", '',
      {
        headers: headers,
        params: params,
        observe: "response"
      })
      .pipe(catchError(this.handleError('stopProject', projectID)))
  }

}
