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
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Login} from "../../models/login.model";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  login: Login;
  loginError: boolean;

  constructor(private http: HttpClient, private auth: AuthService, private route: ActivatedRoute, private router: Router) {

  }

  ngOnInit() {
    this.login = new Login();
    this.route.queryParams.subscribe(params => {
      this.loginError = params['error'];
      console.log("Error present? " + this.loginError);
    });
    this.route.url.subscribe(url => {
      console.log(url);
    })

  }

  onSubmit() {
    let body = new URLSearchParams();
    body.set('username', this.login.username);
    body.set('password', this.login.password);


    this.http.post('login', body.toString(), {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'), responseType: 'text'
    }).subscribe(() => {
      console.log("test");
      this.auth.getAuthStatusAtLogin(this.login.username);
    });
  }

  registerUser() {
    this.router.navigateByUrl("/register");
  }

  loginUser(event, form) {
    if (event.key === "Enter" && form.valid) {
      this.onSubmit();
    }

  }
}
