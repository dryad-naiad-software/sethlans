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
import {Log} from "../../../models/log.model";
import {HttpClient} from "@angular/common/http";
import {Subject} from "rxjs/Subject";

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  logList: Log[];
  dtOptions: DataTables.Settings = {};
  dtTrigger: Subject<any> = new Subject();


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.dtOptions = {
      order: [[0, "desc"]]
    };
    this.http.get('/api/management/get_logs/').subscribe((logList: Log[]) => {
      this.logList = logList;
      this.dtTrigger.next();
    });

  }

  reloadLogs() {
    window.location.href = "/admin/logs";
  }

}
