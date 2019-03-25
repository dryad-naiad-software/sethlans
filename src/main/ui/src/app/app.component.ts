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
import {HttpClient} from '@angular/common/http';
import {Mode} from './enums/mode.enum';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  firstTime: boolean;
  mode: any = Mode;
  sethlansVersion: string;
  javaVersion;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/info/first_time').subscribe((firstTime: boolean) => this.firstTime = firstTime);
    this.http.get('/api/info/version').subscribe((version) => this.sethlansVersion = version['version']);
    this.http.get('/api/info/sethlans_mode').subscribe((sethlansmode) => this.mode = sethlansmode['mode']);
    this.http.get('/api/info/java_version').subscribe((java_version) => this.javaVersion = java_version['java_version']);
  }
}
