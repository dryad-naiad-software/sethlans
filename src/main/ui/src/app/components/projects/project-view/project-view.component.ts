/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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
import {timer} from 'rxjs';
import {ProjectType} from '../../../enums/project_type.enum';
import {BlenderEngine} from '../../../enums/blender_engine.enum';
import {ComputeMethod} from '../../../enums/compute.method.enum';
import {ProjectEditVideoSettingsComponent} from '../project-edit-video-settings/project-edit-video-settings.component';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {AnimationType} from '../../../enums/animation_type.enum';
import {VideoCodec} from '../../../enums/video_codec.enum';

@Component({
  selector: 'app-project-view',
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.scss']
})
export class ProjectViewComponent implements OnInit {
  currentProject: Project;
  id: number;
  statuses: any = ProjectStatus;
  projectTypes: any = ProjectType;
  codecs: any = VideoCodec;
  engines: any = BlenderEngine;
  computeMethods: any = ComputeMethod;
  totalRenderTime: string;
  projectTime: string;
  totalQueue: number;
  remainingQueue: number;
  thumbnailStatus: boolean;
  completedFrames: number;
  stillImage: any;
  animationTypes: any = AnimationType;
  currentThumbnail: any;
  placeholder: any = 'assets/images/placeholder.svg';
  nodesReady: boolean = false;
  disableButton: boolean;
  @ViewChild(ProjectEditVideoSettingsComponent) videoSettings: ProjectEditVideoSettingsComponent;


  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute, private modalService: NgbModal) {
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
      this.getProjectStillImage();
      this.getCompletedFrames();
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

  editProject(id) {
    window.location.href = '/projects/edit/' + id;
  }

  getCompletedFrames() {
    this.http.get('/api/project_ui/completed_frames/' + this.id + '/').subscribe((completed: number) => {
      this.completedFrames = completed;
    });
  }

  getThumbnailStatus() {
    this.http.get('/api/project_ui/thumbnail_status/' + this.id + '/').subscribe((thumbnailStatus: boolean) => {
      this.thumbnailStatus = thumbnailStatus;
      if (thumbnailStatus == true) {
        this.currentThumbnail = '/api/project_ui/current_thumbnail/' + this.id + '/';
      }
    });
  }

  getProjectStillImage() {
    if(this.currentProject.projectStatus == ProjectStatus.Finished) {
      this.stillImage = '/api/project_ui/still_image/' + this.id + '/';
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

  stopProject(id) {
    this.http.get('/api/project_actions/stop_project/' + id + '/').subscribe();
  }

  downloadProject(id) {
    window.location.href = '/api/project_actions/download_project/' + id;
  }

  downloadVideo(id) {
    window.location.href = '/api/project_actions/download_project_video/' + id;
  }

  projectQueue(id) {
    window.location.href = '/projects/queue/' + id;
  }

  frameView(id) {
    window.location.href = '/projects/frames/' + id;
  }

  confirm(content) {
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }
}
