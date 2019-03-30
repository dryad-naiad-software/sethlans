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

import {Component, OnInit} from '@angular/core';
import {Project} from '../../../models/project.model';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-project-frames',
  templateUrl: './project-frames.component.html',
  styleUrls: ['./project-frames.component.scss']
})
export class ProjectFramesComponent implements OnInit {
  currentProject: Project;
  id: number;
  frameIds: number[] = [];


  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) {
    this.currentProject = new Project();
    this.route.params.subscribe(params => {
      this.id = +params['id'];
    });
  }

  ngOnInit() {
    this.loadProjectDetails();
  }

  loadProjectDetails() {
    this.http.get('/api/project_ui/project_details/' + this.id + '/').subscribe((projectDetails: Project) => {
      this.currentProject = projectDetails;
      this.http.get('/api/project_ui/completed_frame_ids/' + this.id + '/').subscribe((frameIdList: number[]) => {
        this.frameIds = frameIdList;
        console.log(this.frameIds);
      });
    });
  }

  refresh() {
    window.location.href = '/projects/frames/' + this.id + '/';
  }

  return() {
    window.location.href = '/projects/view/' + this.id + '/';
  }

}
