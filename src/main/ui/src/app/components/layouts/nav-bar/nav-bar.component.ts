/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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
import {SethlansNotification} from '../../../models/sethlans_notification.model';
import {NotificationType} from '../../../enums/notification_type.enum';

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
  @Input() javaVersion: string;
  mode: any = Mode;
  notificationList: SethlansNotification[] = [];
  notificationListDisplay: SethlansNotification[] = [];
  notifications: boolean;
  scrollDistance = 1;
  throttle = 300;
  slice = 10;
  direction = 'down';
  role: any = Role;
  username: string;
  isCollapsed = true;
  newNotifications: boolean;
  numOfNewNotifications: number;
  notificationTypes: any = NotificationType;


  constructor(private http: HttpClient, private modalService: NgbModal) {
    this.authenticated = false;
    this.username = "";
    this.newNotifications = false;
  }

  ngOnInit() {
    if (!this.firstTime) {
      this.http.get('/api/users/is_authenticated').subscribe((response: boolean) => {
        if (response) {
          this.authenticated = response;
          if (response) {
            this.getUserName();
            this.getAdminStatus();
            this.checkNotifications();
            let scheduler = timer(5000, 5000);
            scheduler.subscribe(() => this.checkNotifications());
          }
        }
      });
    }
  }

  getNotifications() {
    this.http.get('/api/notifications/get_notifications').subscribe((notifications: SethlansNotification[]) => {
      this.notificationList = [];
      if (notifications != null) {
        this.notificationList = notifications.reverse();
        this.notificationListDisplay = this.notificationList.slice(0, this.slice);
      }
    });
  }

  onScrollDown() {
    if (this.notificationListDisplay.length < this.notificationList.length) {
      let len = this.notificationListDisplay.length;
      let maxLength = this.notificationList.length;

      if (len + 10 < maxLength) {
        for (let i = len; i <= len + 10; i++) {
          this.slice++;
          this.getNotifications();
        }
      } else {
        for (let i = len; i < maxLength; i++) {
          this.slice++;
          this.getNotifications();
        }
      }


    }
  }


  open(content) {
    this.modalService.open(content);
  }

  checkNotifications() {
    this.http.get('/api/notifications/new_notifications_present').subscribe((newNotification: boolean) => {
      this.newNotifications = newNotification;
      this.http.get('/api/notifications/number_of_new_notifications').subscribe((numberOfNotifications: number) => {
        this.numOfNewNotifications = numberOfNotifications;
      });
    });
    this.http.get('/api/notifications/notifications_present').subscribe((present: boolean) => {
      this.notifications = present;
      if (present == true) {
        this.getNotifications();
      }
    });
  }

  acknowledgeNotification(id: number) {
    this.http.get('/api/notifications/acknowledge_notification/' + id).subscribe((acknowledged: boolean) => {
      if (acknowledged) {
        this.checkNotifications();
      }
    });
  }

  followLink(notification: SethlansNotification) {
    this.http.get('/api/notifications/acknowledge_notification/' + notification.id).subscribe((acknowledged: boolean) => {
      if (acknowledged) {
        window.location.href = notification.messageLink;
      }
    });

  }

  clearAllNotifications() {
    this.http.get('/api/notifications/clear_all_notifications/').subscribe((acknowledged: boolean) => {
      if (acknowledged) {
        this.checkNotifications();
      }
    });
  }

  clearNotification(id) {
    this.http.get('/api/notifications/clear_notification/' + id).subscribe((cleared: boolean) => {
      if (cleared) {
        this.checkNotifications();
      }
    });
  }

  acknowledgeAllNotifications() {
    this.http.get('/api/notifications/acknowledge_all_notifications/').subscribe((acknowledged: boolean) => {
      if (acknowledged) {
        this.checkNotifications();
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
