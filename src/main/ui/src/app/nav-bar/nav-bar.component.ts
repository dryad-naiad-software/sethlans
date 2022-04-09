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

import {Component, OnInit} from '@angular/core';
import {
  faArrowsRotate,
  faBell,
  faBook,
  faBookOpen,
  faCogs,
  faDesktop,
  faGear,
  faHourglassStart,
  faHouse,
  faMicrochip,
  faPowerOff,
  faPuzzlePiece,
  faQuestionCircle,
  faRightFromBracket,
  faServer,
  faStar,
  faTimeline,
  faUser,
  faWrench
} from '@fortawesome/free-solid-svg-icons';
import {SethlansService} from "../services/sethlans.service";
import {Mode} from "../enums/mode.enum";
import {UserQuery} from "../models/user/userquery.model";
import {Role} from "../enums/role.enum";

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.css']
})
export class NavBarComponent implements OnInit {
  firstTime: boolean = false;
  logo: any = 'assets/images/logo-text-white.png';
  logoDark: any = 'assets/images/logo-text-dark.png';
  authenticated: boolean = false;
  user: UserQuery | undefined;
  isAdministrator = false;
  isSuperAdministrator = false;
  faHouse = faHouse;
  faUser = faUser;
  faWrench = faWrench;
  faBookOpen = faBookOpen;
  faStar = faStar;
  faBell = faBell;
  faGear = faGear;
  faServer = faServer;
  faPowerOff = faPowerOff;
  faMicrochip = faMicrochip;
  faHourglassStart = faHourglassStart
  faQuestionCircle = faQuestionCircle;
  faRightFromBracket = faRightFromBracket;
  faPuzzlePiece = faPuzzlePiece;
  faTimeLine = faTimeline;
  faDesktop = faDesktop;
  faArrowsRotate = faArrowsRotate;
  faBook = faBook;
  faCogs = faCogs;
  newNotifications = false;
  mode: Mode = Mode.SETUP;
  Mode = Mode;


  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.isFirstTime().subscribe((data: any) => {
      this.firstTime = data.first_time;
    })
    this.sethlansService.isAuthenticated().subscribe((data: any) => {
      this.authenticated = data.authenticated;
    });
    this.sethlansService.getCurrentUser().subscribe((data: any) => {
      this.user = data;
      if (this.user?.roles.indexOf(Role.ADMINISTRATOR) !== -1
        || this.user?.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
        this.isAdministrator = true;
      }
      if (this.user?.roles.indexOf(Role.SUPER_ADMINISTRATOR) !== -1) {
        this.isSuperAdministrator = true;
      }
    });
    this.sethlansService.mode().subscribe((data: any) => {
      this.mode = data.mode;
    });
  }

}
