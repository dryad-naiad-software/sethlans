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
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {Project} from "../../models/project.model";
import {ProjectListService} from "../../services/project_list.service";
import {Subject} from "rxjs/Subject";
import {DataTableDirective} from "angular-datatables";
import {Router} from "@angular/router";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit, AfterViewInit {
  @ViewChild(DataTableDirective)
  dtElement: DataTableDirective;
  dtTrigger: Subject<any> = new Subject();
  placeholder: any = "assets/images/placeholder.svg";
  nodesReady: boolean = false;
  projectSize: number;
  projects: Project[];
  dtOptions: DataTables.Settings = {};

  constructor(private http: HttpClient, private projectService: ProjectListService, private router: Router) {
  }

  ngAfterViewInit(): void {
    this.projectService.getProjectList().subscribe(value => {
      this.projects = value;
      this.dtTrigger.next();
    });
  }

  ngOnInit() {
    this.getNodeStatus();
    this.getProjectListSize();
    this.dtOptions = {
    };


    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.getNodeStatus();
      this.getProjectListSize();
    });

    let timer2 = Observable.timer(0, 60000);
    timer2.subscribe(() => {
      this.rerender();
    })
  }


  startProject(id) {
    this.http.get("/api/project_actions/start_project/" + id + "/").subscribe((success: boolean) => {
      if (success) {
        this.router.navigateByUrl("/projects").then(() => location.reload());
      }
    });
  }

  getProjectListSize() {
    this.http.get<number>("/api/project_ui/num_of_projects").subscribe((projectSize: number) => {
      this.projectSize = projectSize;
    });
  }


  getNodeStatus() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });
  }


  rerender(): void {
    this.dtElement.dtInstance.then((dtInstance: DataTables.Api) => {
      // Destroy the table first
      dtInstance.destroy();
      this.projectService.getProjectList().subscribe(value => {
        this.projects = value;
        this.dtTrigger.next();
      });
    });
  }

  addProject() {
    this.router.navigateByUrl("/projects/add");
  }

  editProject(id) {
    this.router.navigate(['/projects/edit', id]);
  }

  viewProject(id) {
    this.router.navigate(['/projects/view', id])
  }

  deleteProject(id) {
    this.http.get('/api/project_actions/delete_project/' + id + '/').subscribe((success: boolean) => {
      if (success == true) {
        this.router.navigateByUrl('/projects').then(() => location.reload());
      }
    })
  }



}
