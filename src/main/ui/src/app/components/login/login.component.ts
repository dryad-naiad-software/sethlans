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
import {LoginService} from '../../services/login.service';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {Login} from '../../models/login.model';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = 'assets/images/logo-text-dark.png';
  login: Login;
  loginError: boolean;

  constructor(private http: HttpClient, private auth: LoginService, private activatedRoute: ActivatedRoute, private router: Router) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';

    this.activatedRoute.queryParams.subscribe(params => {
      let loginError = params['error'];
      this.loginError = loginError != undefined;
    });
  }

  ngOnInit() {
    this.login = new Login();
  }

  loginSubmit() {
    this.auth.authenticate(this.login, () => {
      this.router.navigateByUrl('/login');
    });

  }

  registerUser() {
    this.router.navigateByUrl('/register');
  }

  loginUser(event, form) {
    if (event.key === 'Enter' && form.valid) {
      this.loginSubmit();
    }
  }

}
