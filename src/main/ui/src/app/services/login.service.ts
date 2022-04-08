/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {SethlansService} from "./sethlans.service";

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  constructor(private http: HttpClient, private sethlansService: SethlansService) {
  }

  authenticate(credentials: { username: string; password: string; }, callback: () => any) {

    let body = new URLSearchParams();
    body.set('username', credentials.username);
    body.set('password', credentials.password);

    this.http.post('login', body.toString(), {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text', observe: 'response'
    }).subscribe((loginresponse) => {
      let url = loginresponse.url;
      this.sethlansService.isAuthenticated().subscribe((authd: any) => {
        if (authd) {
          // @ts-ignore
          window.location.href = url;
        }
      })
      setTimeout(function () {
        return callback && callback();
      }, 1000);

    });

  }
}
