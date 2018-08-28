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
        if (response) {
          this.getUserName();
          this.getAdminStatus();
          this.checkNotifications();
          let scheduler = timer(5000, 2000);
          scheduler.subscribe(() => this.checkNotifications());
        }
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
    this.http.get('/api/notifications/notifications_present').subscribe((present: boolean) => {
      this.notifications = present;
      if (present == true) {
        this.getNotifications();
      }
    });
  }

  getAdminStatus() {
    this.http.get('/api/users/is_administrator').subscribe((admin: boolean) => {
      this.isAdministrator = admin;
    });
    this.http.get('/api/users/is_super_administrator').subscribe((superAdmin: boolean) => {
      this.isSuperAdministrator = superAdmin;
    });
  }


  getUserName() {
    this.http.get('/api/users/username')
      .subscribe((user) => {
        this.authenticated = true;
        this.username = user['username'];
      });
  }


}
