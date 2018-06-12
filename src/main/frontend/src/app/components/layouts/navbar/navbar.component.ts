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
import {HttpClient} from '@angular/common/http';
import {Role} from '../../../enums/role.enum';
import {UserInfo} from '../../../models/userinfo.model';
import {Mode} from '../../../enums/mode.enum';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Observable} from 'rxjs/Observable';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  logo: any = 'assets/images/logo.png';
  logoDark: any = 'assets/images/logo-dark.png';
  username: string;
  authenticated: boolean;
  isCollapsed = true;
  userInfo: UserInfo;
  role: any = Role;
  isAdministrator = false;
  isSuperAdministrator = false;
  currentMode: Mode;
  mode: any = Mode;
  sethlansVersion: string;
  notifications: boolean;
  notificationList: string[];

  constructor(private http: HttpClient, private modalService: NgbModal) {
  }


  ngOnInit() {
    this.checkNotifications();
    let timer = Observable.timer(5000, 2000);
    timer.subscribe(() => this.checkNotifications());
    this.getVersion();
    this.getMode();
    this.getUserName();
  }

  getNotifications() {
    this.http.get('/api/notifications/get_notifications').subscribe((notifications: string[]) => {
      this.notificationList = notifications;
    });
  }

  open(content) {
    this.modalService.open(content);
  }

  checkNotifications() {
    this.http.get('/api/notifications/notificiations_present').subscribe((present: boolean) => {
      this.notifications = present;
      if (present == true) {
        this.getNotifications();
      }
    });
  }

  getMode() {
    this.http.get('/api/info/sethlans_mode')
      .subscribe((sethlansmode) => {
        this.currentMode = sethlansmode['mode'];
      });
  }

  getUserName() {
    this.http.get('/api/users/username')
      .subscribe((user) => {
        if (user['username'].indexOf('<') >= 0) {
          this.authenticated = false;
        }
        else {
          this.authenticated = true;
          this.username = user['username'];
          this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
            this.userInfo = userinfo;
            console.log(this.userInfo);
            if (userinfo.roles.indexOf(Role.ADMINISTRATOR) !== -1 || userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
              this.isAdministrator = true;
            }
            if (userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
              this.isSuperAdministrator = true;
            }
          });
        }

      });
  }

  getVersion() {
    this.http.get('/api/info/version')
      .subscribe((version) => {
        this.sethlansVersion = version['version'];
      });
  }

}
