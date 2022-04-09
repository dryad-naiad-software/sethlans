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
import {SethlansService} from "../../services/sethlans.service";
import {Mode} from "../../enums/mode.enum";

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
  mode: Mode = Mode.SETUP;
  sethlansVersion: string = "";
  year: string = "";

  constructor(private sethlansService: SethlansService) {
  }

  ngOnInit(): void {
    this.sethlansService.version().subscribe((data: any) => {
      this.sethlansVersion = data.version;
    });
    this.sethlansService.mode().subscribe((data: any) => {
      this.mode = data.mode;
    });
    this.sethlansService.year().subscribe((data: any) => {
      this.year = data.year;
    });
  }

}
