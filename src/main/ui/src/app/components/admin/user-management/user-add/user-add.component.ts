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
import {User} from '../../../../models/user.model';
import {Role} from '../../../../enums/role.enum';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Router} from '@angular/router';
import {UserInfo} from '../../../../models/user_info.model';

@Component({
  selector: 'app-user-add',
  templateUrl: './user-add.component.html',
  styleUrls: ['./user-add.component.scss']
})
export class UserAddComponent implements OnInit {
  user: User;
  userExists: boolean;
  userInfo: UserInfo;
  username: string;
  isSuperAdministrator = false;
  isAdministrator = false;
  existingUserName: string;
  roles = Object.keys(Role);
  limitedRoles = [Role.ADMINISTRATOR, Role.USER];

  constructor(private http: HttpClient, private router: Router) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.userInfo = new UserInfo();
    this.user = new User();
    this.user.roles = [];
  }

  ngOnInit() {
    this.getUserName();

  }

  getUserName() {
    this.http.get('/api/users/username')
      .subscribe((user) => {
        this.username = user['username'];
        this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
          if (userinfo.roles.indexOf(Role.ADMINISTRATOR) !== -1 || userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
            this.isAdministrator = true;
            this.isSuperAdministrator = false;
          }
          if (userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
            this.isSuperAdministrator = true;
            this.isAdministrator = false;
          }
        });
      });
  }

  selected(event, string) {
    let checked = event.currentTarget.checked;
    if (checked) {
      let newUser = this.user;
      this.roles.forEach(function (value: Role) {
        if (value.valueOf() == string) {
          newUser.roles.push(value);
        }
      });
    } else if (!checked) {
      let selectedRoles = this.user.roles;
      for (let i = 0; i < selectedRoles.length; i++) {
        if (selectedRoles[i].valueOf() == string) {
          this.user.roles.splice(i, 1);
        }
      }
    }
  }

  submitUser(event, form) {
    if (event.key === 'Enter' && form.valid) {
      this.onSubmit();
    }

  }

  onSubmit() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post('/api/management/add_user', JSON.stringify(this.user), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        window.location.href = '/admin/user_management';
      } else {
        this.router.navigateByUrl('/admin/user_management/add?error=true&username=' + this.user.username).then(() => {
          location.reload();
        });
      }
    });
  }

}
