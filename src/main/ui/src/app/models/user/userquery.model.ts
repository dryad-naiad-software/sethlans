/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {UserChallenge} from "./user-challenge.model";
import {Role} from "../../enums/role.enum";
import {NotificationSetttings} from "../settings/notificationsettings.model";

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class UserQuery {
  userID: string;
  username: string;
  email: string;
  active: boolean;
  notificationSettings: NotificationSetttings;
  challengeList: Array<UserChallenge>;
  roles: Array<Role>;

  constructor() {
    this.userID = '';
    this.username = '';
    this.email = '';
    this.active = false;
    this.notificationSettings = new NotificationSetttings();

    this.challengeList = new Array<UserChallenge>();
    this.roles = new Array<Role>();
  }

  setUserQuery(obj: any) {
    this.userID = obj.userID;
    this.username = obj.username;
    this.email = obj.email;
    this.active = obj.active;
    this.notificationSettings = obj.notificationSettings;
    this.challengeList = obj.challengeList;
    this.roles = obj.roles;
  }
}
