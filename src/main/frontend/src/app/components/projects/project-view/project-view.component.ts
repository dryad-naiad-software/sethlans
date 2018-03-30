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
import {Project} from "../../../models/project.model";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {ProjectStatus} from "../../../enums/project_status.enum";

@Component({
  selector: 'app-project-view',
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.scss']
})
export class ProjectViewComponent implements OnInit {
  projectDetails: Project;
  id: number;
  projectLoaded: boolean = false;
  placeholder: any = "assets/images/placeholder.svg";
  currentProgress: number;
  projectStatus: ProjectStatus;
  currentThumbnail: any;
  thumbnailStatus: boolean;



  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.id = +params['id'];
      this.loadProjectDetails();
      this.currentProgressCheck();
      this.currentStatusCheck();
      this.getThumbnailStatus();
    });
    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => {
      this.currentProgressCheck();
      this.currentStatusCheck();
      this.getThumbnailStatus();
    });

  }

  loadProjectDetails() {
    this.http.get('/api/project_ui/project_details/' + this.id + '/').subscribe((projectDetails: Project) => {
      this.projectDetails = projectDetails;
      this.projectLoaded = true;
      console.log(projectDetails);
    });
  }

  currentProgressCheck() {
    this.http.get('/api/project_ui/progress/' + this.id + '/').subscribe((currentProgress: number) => {
      this.currentProgress = currentProgress;
    })
  }

  currentStatusCheck() {
    this.http.get('/api/project_ui/status/' + this.id + '/').subscribe((currentStatus: ProjectStatus) => {
      this.projectStatus = currentStatus;
    })
  }

  returnToProjects(): void {
    this.router.navigateByUrl("/projects").then(() => location.reload());
  }

  getThumbnailStatus() {
    this.http.get('/api/project_ui/thumbnail_status/' + this.id + '/').subscribe((thumbnailStatus: boolean) => {
      this.thumbnailStatus = thumbnailStatus;
      if (thumbnailStatus == true) {
        this.currentThumbnail = '/api/project_ui/thumbnail/' + this.id + '/';
      }
    })
  }

}
