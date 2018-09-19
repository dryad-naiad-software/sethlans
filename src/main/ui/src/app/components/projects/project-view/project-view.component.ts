/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import {Component, OnInit, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {Project} from '../../../models/project.model';
import {ProjectStatus} from '../../../enums/project_status.enum';
import {RenderOutputFormat} from '../../../enums/render_output_format.enum';
import {timer} from 'rxjs';
import {ProjectType} from '../../../enums/project_type.enum';
import {BlenderEngine} from '../../../enums/blender_engine.enum';
import {ComputeMethod} from '../../../enums/compute.method.enum';
import {ProjectVideoSettingsComponent} from '../project-video-settings/project-video-settings.component';

@Component({
  selector: 'app-project-view',
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.scss']
})
export class ProjectViewComponent implements OnInit {
  currentProject: Project;
  id: number;
  statuses: any = ProjectStatus;
  formats: any = RenderOutputFormat;
  projectTypes: any = ProjectType;
  engines: any = BlenderEngine;
  computeMethods: any = ComputeMethod;
  totalRenderTime: string;
  projectTime: string;
  totalQueue: number;
  remainingQueue: number;
  thumbnailStatus: boolean;
  modalImage: any;
  currentThumbnail: any;
  placeholder: any = 'assets/images/placeholder.svg';
  nodesReady: boolean = false;
  disableButton: boolean;
  @ViewChild(ProjectVideoSettingsComponent) videoSettings: ProjectVideoSettingsComponent;


  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) {
    this.currentProject = new Project();
    this.route.params.subscribe(params => {
      this.id = +params['id'];
    });
  }

  ngOnInit() {
    this.loadProjectDetails();
    let scheduler = timer(5000, 5000);
    scheduler.subscribe(() => this.loadProjectDetails());
  }

  loadProjectDetails() {
    this.http.get('/api/project_ui/project_details/' + this.id + '/').subscribe((projectDetails: Project) => {
      this.currentProject = projectDetails;
      this.getTotalRenderTime();
      this.getProjectTime();
      this.getTotalQueueSize();
      this.getRemainingQueueSize();
      this.getThumbnailStatus();
      this.getNodeStatus();
      this.getProjectModalImage();
      this.disableButton = false;
    });
  }


  getTotalQueueSize() {
    this.http.get('/api/project_ui/total_queue/' + this.id + '/').subscribe((totalQueue: number) => {
      this.totalQueue = totalQueue;
    });
  }

  getRemainingQueueSize() {
    this.http.get('/api/project_ui/remaining_queue/' + this.id + '/').subscribe((remainingQueue: number) => {
      this.remainingQueue = remainingQueue;
    });
  }

  getTotalRenderTime() {
    this.http.get('/api/project_ui/render_time/' + this.id + '/', {responseType: 'text'}).subscribe((renderTime: string) => {
      this.totalRenderTime = renderTime;
    });
  }


  getProjectTime() {
    this.http.get('/api/project_ui/project_duration/' + this.id + '/', {responseType: 'text'}).subscribe((duration: string) => {
      this.projectTime = duration;
    });
  }

  getThumbnailStatus() {
    this.http.get('/api/project_ui/thumbnail_status/' + this.id + '/').subscribe((thumbnailStatus: boolean) => {
      this.thumbnailStatus = thumbnailStatus;
      if (thumbnailStatus == true) {
        this.currentThumbnail = '/api/project_ui/thumbnail/' + this.id + '/';
      }
    });
  }

  getProjectModalImage(){
    if(this.currentProject.projectStatus == ProjectStatus.Finished) {
      this.modalImage = '/api/project_ui/modal_image/' + this.id + '/';
    }
  }

  getNodeStatus() {
    this.http.get('/api/project_ui/nodes_ready').subscribe((success: boolean) => {
      if (success == true) {
        this.nodesReady = true;
      }
    });
  }

  startProject(id) {
    this.disableButton = true;
    this.http.get('/api/project_actions/start_project/' + id + '/').subscribe();

  }

  pauseProject(id) {
    this.disableButton = true;
    this.http.get('/api/project_actions/pause_project/' + id + '/').subscribe();
  }

  resumeProject(id) {
    this.disableButton = true;
    this.http.get('/api/project_actions/resume_project/' + id + '/').subscribe();
  }


  returnToProjects(): void {
    window.location.href = '/projects';
  }

}
