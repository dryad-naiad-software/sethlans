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

import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Mode} from '../../../enums/mode.enum';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {UserInfo} from '../../../models/user_info.model';
import {Role} from '../../../enums/role.enum';
import {timer} from 'rxjs/internal/observable/timer';

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.scss'],
})
export class NavBarComponent implements OnInit {
  logo: any = 'assets/images/logo-text-white.png';
  logoDark: any = 'assets/images/logo-text-dark.png';
  authenticated: boolean;
  isAdministrator = false;
  isSuperAdministrator = false;
  @Input() firstTime: boolean;
  @Input() currentMode: Mode;
  @Input() sethlansVersion: string;
  mode: any = Mode;
  notificationList: string[];
  notifications: boolean;
  userInfo: UserInfo;
  role: any = Role;
  username: string;
  isCollapsed = true;


  constructor(private http: HttpClient, private modalService: NgbModal) {
    this.authenticated = false;
    this.username = "";
  }

  ngOnInit() {
    this.http.get('/api/users/is_authenticated').subscribe((response: boolean) => {
      if (response) {
        this.authenticated = response;
        this.getUserName();
        this.checkNotifications();
        let scheduler = timer(5000, 2000);
        scheduler.subscribe(() => this.checkNotifications());
      }
    });

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


  getUserName() {
    this.http.get('/api/users/username')
      .subscribe((user) => {
        this.authenticated = true;
        this.username = user['username'];
        this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
          if (userinfo.roles.indexOf(Role.ADMINISTRATOR) !== -1 || userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
            this.isAdministrator = true;
          }
          if (userinfo.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
            this.isSuperAdministrator = true;
          }
        });


      });
  }


}
