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
import {MatPaginator, MatTableDataSource} from '@angular/material';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {BlenderBinaryInfo} from '../../../models/blender_binary_info.model';

@Component({
  selector: 'app-blender-versions',
  templateUrl: './blender-versions.component.html',
  styleUrls: ['./blender-versions.component.scss']
})
export class BlenderVersionsComponent implements OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  availableBlenderVersions: any[] = [];
  selectedVersion: string;
  dataSource = new MatTableDataSource();
  displayedColumns = ['version', 'binaries'];


  constructor(private http: HttpClient, private modalService: NgbModal) {
  }

  ngOnInit() {
    this.http.get('/api/management/get_blender_list/').subscribe((blenderList: BlenderBinaryInfo[]) => {
      this.dataSource = new MatTableDataSource<any>(blenderList);
      this.dataSource.paginator = this.paginator;
    });
    this.http.get('/api/management/remaining_blender_versions/')
      .subscribe(
        (blenderVersions: string[]) => {
          this.availableBlenderVersions = blenderVersions;
          if (this.availableBlenderVersions.length > 0) {
            this.selectedVersion = this.availableBlenderVersions[0];
          }
        }, (error) => console.log(error));
  }

  open(content) {
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }


  addVersion() {
    let versionProperty = new HttpParams().set('version', this.selectedVersion.toString());
    this.http.post('/api/setup/add_blender_version', versionProperty, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/admin/blender_version_admin/';
      }
    });
  }


}
