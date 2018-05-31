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
import {User} from "../../../../models/user.model";
import {Role} from "../../../../enums/role.enum";

@Component({
  selector: 'app-user-add',
  templateUrl: './user-add.component.html',
  styleUrls: ['./user-add.component.scss']
})
export class UserAddComponent implements OnInit {
  user: User;
  userExists: boolean;
  existingUserName: string;
  roles = Object.keys(Role);

  constructor() {
  }

  ngOnInit() {
    this.user = new User();
    this.user.roles = [];

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

}
