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
import {User} from "../../models/user.model";
import {Role} from "../../enums/role.enum";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Component({
  selector: 'app-register-user',
  templateUrl: './register-user.component.html',
  styleUrls: ['./register-user.component.scss']
})
export class RegisterUserComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  user: User;
  userExists: boolean;
  existingUserName: string;

  constructor(private router: Router, private http: HttpClient, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.user = new User();
    this.route.queryParams.subscribe(params => {
      this.userExists = params['error'];
      this.existingUserName = params['username'];
    });

  }

  login() {
    this.router.navigateByUrl("/login");
  }

  submitUser(event, form) {
    if (event.key === "Enter" && form.valid) {
      this.onSubmit();
    }

  }

  onSubmit() {
    this.user.setPasswordUpdated(true);
    this.user.setRoles([Role.USER]);
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post("/api/setup/register", JSON.stringify(this.user), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        this.login()
      } else {
        this.router.navigateByUrl("/register?error=true&username=" + this.user.getUserName()).then(() => {
          location.reload();
        });
      }
    });
  }

}
