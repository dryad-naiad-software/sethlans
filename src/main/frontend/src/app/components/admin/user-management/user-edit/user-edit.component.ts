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
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute} from "@angular/router";
import {UserInfo} from "../../../../models/userinfo.model";

@Component({
  selector: 'app-user-edit',
  templateUrl: './user-edit.component.html',
  styleUrls: ['./user-edit.component.scss']
})
export class UserEditComponent implements OnInit {
  id: number;
  userInfo: UserInfo;
  changePass: boolean = false;
  passFields: PasswordSet;


  constructor(private http: HttpClient, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.passFields = new PasswordSet();
    this.route.params.subscribe(params => {
      this.id = +params['id'];
      this.loadUser();
    });
  }

  loadUser() {
    this.http.get('/api/management/get_user/' + this.id + '/').subscribe((userInfo: UserInfo) => {
      this.userInfo = userInfo;
    });
  }

}


class PasswordSet {
  currentPass: string;
  newPass: string;
  newPassConfirm: string;
}
