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
import {Subject} from "rxjs/Subject";
import {ServerInfo} from "../../../models/server_info.model";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/timer";

@Component({
  selector: 'app-servers',
  templateUrl: './servers.component.html',
  styleUrls: ['./servers.component.scss']
})
export class ServersComponent implements OnInit {
  dtTrigger: Subject<any> = new Subject();
  serverList: ServerInfo[] = [];
  ackClicked: boolean = false;
  serverScanComplete: boolean = false;
  dtOptions: DataTables.Settings = {};


  constructor(private http: HttpClient, private router: Router) {
  }

  ngOnInit() {
    this.populateServerList();
    this.dtOptions = {
      searching: false,
      ordering: false
    };
    let timer = Observable.timer(5000, 1000);
    timer.subscribe(() => this.populateServerList());
  }

  populateServerList() {
    this.http.get('/api/management/server_list')
      .subscribe((servers: ServerInfo[]) => {
        this.serverList = servers;
        this.serverScanComplete = true;
      });
  }

  acknowledgeServer(id) {
    this.http.get('/api/setup/server_acknowledge/' + id + "/").subscribe((success: boolean) => {
      if (success == true) {
        this.ackClicked = true;
      }

    });
  }


  deleteServer(id) {
    this.http.get('/api/setup/server_delete/' + id + "/", {responseType: 'text'}).subscribe((success: any) => {
      console.log(success);
      this.router.navigateByUrl("/admin/servers").then(() => {
        location.reload();
      });
    });
  }

}
