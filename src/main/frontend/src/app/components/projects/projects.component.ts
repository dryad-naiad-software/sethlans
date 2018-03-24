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
import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {Project} from "../../models/project.model";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  placeholder: any = "assets/images/placeholder.svg";
  nodesReady: boolean = false;
  projects: Project[] = [];
  projectLoadComplete: boolean = false;


  constructor(private http: HttpClient) {
  }


  ngOnInit() {
    this.getNodeStatus();
    this.getProjectList();
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getNodeStatus();
    });
  }


  getProjectList() {
    this.http.get('/api/project_ui/project_list').subscribe((projects: Project[]) => {
      this.projects = [];
      for (let project of projects) {
        this.projects.push(project);
      }
      this.projectLoadComplete = true;
    });
  }

  getNodeStatus() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });
  }

}
