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
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {VideoForm} from '../../../models/forms/video_form.model';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {Project} from '../../../models/project.model';

@Component({
  selector: 'app-project-video-settings',
  templateUrl: './project-video-settings.component.html',
  styleUrls: ['./project-video-settings.component.scss']
})
export class ProjectVideoSettingsComponent implements OnInit {
  @ViewChild('videoSettings') modal: any;
  videoForm: VideoForm;
  frameRates: string[] = ['23.98', '24', '25', '29.97', '30', '50', '59.94', '60'];
  formats: string[] = ['AVI', 'MP4'];
  project: Project;

  constructor(private http: HttpClient, private modalService: NgbModal) {
  }

  open(project: Project) {
    this.project = project;
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.videoForm = new VideoForm();
    this.videoForm.outputFormat = this.project.outputFormat;
    this.videoForm.frameRate = this.project.frameRate;
    this.modalService.open(this.modal, options);
  }

  ngOnInit() {
  }

  submitChanges() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/project_form/video_settings/' + this.project.id, JSON.stringify(this.videoForm), httpOptions).subscribe();
  }

}
