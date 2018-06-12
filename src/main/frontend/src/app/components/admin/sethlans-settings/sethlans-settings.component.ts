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
import {HttpClient} from '@angular/common/http';
import {SethlansConfig} from '../../../models/sethlans_config.model';
import {Mode} from '../../../enums/mode.enum';
import {Router} from '@angular/router';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-sethlans-settings',
  templateUrl: './sethlans-settings.component.html',
  styleUrls: ['./sethlans-settings.component.scss']
})
export class SethlansSettingsComponent implements OnInit {
  sethlansConfig: SethlansConfig;
  mode: any = Mode;
  newSettings = false;

  constructor(private http: HttpClient, private router: Router, private modalService: NgbModal) {
  }

  ngOnInit() {
    this.http.get('/api/management/current_settings').subscribe((sethlansConfig: SethlansConfig) => {
      this.sethlansConfig = sethlansConfig;
      console.log(this.sethlansConfig);
    });
  }

  open(content) {
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }


}
