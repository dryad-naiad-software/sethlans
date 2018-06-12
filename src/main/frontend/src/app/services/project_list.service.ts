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
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Project} from '../models/project.model';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class ProjectListService {

  constructor(private http: HttpClient) {
  }

  getProjectList(): Observable<Project[]> {
    return this.http.get<Project[]>('/api/project_ui/project_list');
  }

  getProjectListInProgress(): Observable<Project[]> {
    return this.http.get<Project[]>('/api/project_ui/project_list_in_progress');
  }

  getProjectListSize(): Observable<number> {
    return this.http.get<number>('/api/project_ui/num_of_projects');
  }


}

