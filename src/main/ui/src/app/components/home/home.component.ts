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

import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Mode} from '../../enums/mode.enum';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  getStarted: boolean;
  mode: any = Mode;
  currentMode: Mode;
  isAdministrator = false;

  constructor(private http: HttpClient) {
    this.getStarted = false;
  }

  ngOnInit() {
    this.http.get('/api/info/sethlans_mode')
      .subscribe((sethlansmode) => {
        this.currentMode = sethlansmode['mode'];
      });
    this.http.get('/api/users/admin_added_user').subscribe((response: boolean) => {
      if (response) {
        window.location.href = '/user_settings?is_new_user=true';
      } else {
        this.http.get('/api/users/prompt_pass_change').subscribe((response: boolean) => {
          if (response) {
            window.location.href = '/user_settings?needs_password_change=true';
          }
        });
        this.http.get('/api/users/is_security_questions_set').subscribe((response: boolean) => {
          if (!response) {
            window.location.href = '/user_settings?needs_questions=true';
          }
        });
      }
    });


    this.http.get('/api/users/is_administrator').subscribe((admin: boolean) => {
      this.isAdministrator = admin;
      if (admin) {
        this.http.get('/api/info/get_started').subscribe((response: boolean) => {
          this.getStarted = response;
          if (this.getStarted) {
            window.location.href = '/get_started';
          }
        });
      }

    });
  }

}
