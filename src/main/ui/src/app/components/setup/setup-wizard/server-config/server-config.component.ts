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

import {Component, Input, OnInit} from '@angular/core';
import {SetupForm} from '../../../../models/setup_form.model';
import {HttpClient} from '@angular/common/http';
import {Server} from '../../../../models/server.model';

@Component({
  selector: 'app-server-config',
  templateUrl: './server-config.component.html',
  styleUrls: ['./server-config.component.scss']
})
export class ServerConfigComponent implements OnInit {
  @Input() setupForm: SetupForm;
  availableBlenderVersions: string[];


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    if (this.setupForm.server == null) {
      this.setupForm.server = new Server();
    }
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions) => {
          this.availableBlenderVersions = blenderVersions['blenderVersions'];
          if (this.setupForm.server.blenderVersion == '') {
            this.setupForm.server.blenderVersion = this.availableBlenderVersions[0];
          }
        });
  }

}
