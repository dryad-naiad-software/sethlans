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
import {ActivatedRoute} from '@angular/router';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-project-edit-video-settings',
  templateUrl: './project-edit-video-settings.component.html',
  styleUrls: ['./project-edit-video-settings.component.scss']
})
export class ProjectEditVideoSettingsComponent implements OnInit {
  currentProject: Project;
  id: number;


  constructor(private http: HttpClient, private route: ActivatedRoute) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.currentProject = new Project();
    this.route.params.subscribe(params => {
      this.id = +params['id'];
    });
  }

  ngOnInit(): void {
    this.loadProjectDetails();
  }

  loadProjectDetails() {
    this.http.get('/api/project_ui/project_details/' + this.id + '/').subscribe((projectDetails: Project) => {
      this.currentProject = projectDetails;
    });
  }

}
