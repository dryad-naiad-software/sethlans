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

import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";

@Injectable()
export class AuthService {
  authenticated;

  constructor(private http: HttpClient, private router: Router) {

  }

  getAuthStatusAtLogin(username) {
    this.http.get('/api/users/username').subscribe((user) => {
      this.authenticated = user['username'].toLowerCase() === username.toLowerCase();
      if (this.authenticated == true) {
        this.router.navigateByUrl("/").then(() => {
          location.reload();

        });
      } else {
        this.authenticated = false;
        this.router.navigateByUrl("/login?error=true");
      }

    });
  }

  getAuthenticated(): boolean {
    return this.authenticated;
  }
}
