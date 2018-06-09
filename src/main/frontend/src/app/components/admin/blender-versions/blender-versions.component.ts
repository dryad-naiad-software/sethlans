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
import {BlenderBinaryInfo} from "../../../models/blenderbinaryinfo.model";

@Component({
  selector: 'app-blender-versions',
  templateUrl: './blender-versions.component.html',
  styleUrls: ['./blender-versions.component.scss']
})
export class BlenderVersionsComponent implements OnInit {
  blenderBinaryList: BlenderBinaryInfo[] = [];
  availableBlenderVersions: any[];

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/management/get_blender_list/').subscribe((blenderList: BlenderBinaryInfo[]) => {
      this.blenderBinaryList = blenderList;
      console.log(this.blenderBinaryList)
    });
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions) => {
          this.availableBlenderVersions = blenderVersions['blenderVersions'];
          console.log(this.availableBlenderVersions);
        }, (error) => console.log(error));
  }

}
