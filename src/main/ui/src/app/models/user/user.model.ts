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

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class User {
  userID: string;
  username: string;
  password: string;
  email: string;
  active: boolean;
  systemEmailNotifications: boolean;
  nodeEmailNotifications: boolean;
  projectEmailNotifications: boolean;
  videoEncodingEmailNotifications: boolean;
  securityQuestionsSet: boolean;
  promptPasswordChange: boolean;
  challengeList: Array<UserChallenge>;
  roles: Array<Role>;


  constructor(obj: any) {
    this.userID = obj.userID;
    this.username = obj.username;
    this.password = obj.password;
    this.securityQuestionsSet = obj.securityQuestionsSet;
    this.email = obj.email;
    this.promptPasswordChange = obj.promptPasswordChange;
    this.active = obj.active;
    this.systemEmailNotifications = obj.systemEmailNotifications;
    this.nodeEmailNotifications = obj.nodeEmailNotifications;
    this.projectEmailNotifications = obj.projectEmailNotifications;
    this.videoEncodingEmailNotifications = obj.videoEncodingEmailNotifications;
    this.challengeList = obj.challengeList;
    this.roles = obj.roles;
  }
}
