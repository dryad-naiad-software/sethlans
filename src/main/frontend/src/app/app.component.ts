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

import {Component, Injectable, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {AuthService} from "./services/auth.service";
import 'rxjs/add/operator/finally';
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

@Injectable()
export class AppComponent implements OnInit {
  title = 'Sethlans';
  firstTime: boolean;
  logo: any = "assets/images/logo.png";

  constructor(private http: HttpClient, private auth: AuthService, private router: Router) {
    this.http.get('/api/info/first_time').subscribe((firstTime: boolean) => this.firstTime = firstTime);
  }

  logout() {
    this.http.post('logout', {}).finally(() => {
      this.auth.authenticated = false;
      this.router.navigateByUrl('/login');
    }).subscribe();
  }

  ngOnInit() {

  }
}
