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

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';

@Injectable()
export class AuthService {
  authenticated = false;

  constructor(private http: HttpClient) {
  }

  authenticate(credentials, callback) {

    let body = new URLSearchParams();
    body.set('username', credentials.username);
    body.set('password', credentials.password);

    this.http.post('login', body.toString(), {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text', observe: 'response'
    }).subscribe((loginresponse) => {
      let url = loginresponse.url;
      console.log(url);
      this.http.get('/api/users/username').subscribe(response => {
        this.authenticated = !!response['username'];
        window.location.href = url;
      });
      setTimeout(function () {
        return callback && callback();
      }, 1000);

    });

  }
}
