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

import {AfterViewInit, Component, OnInit} from '@angular/core';
import {ServerInfo} from "../../../models/server_info.model";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/timer";
import {ServerListService} from "../../../services/server_list.service";

@Component({
  selector: 'app-servers',
  templateUrl: './servers.component.html',
  styleUrls: ['./servers.component.scss']
})
export class ServersComponent implements OnInit, AfterViewInit {
  serverSize: number;
  serverList: ServerInfo[] = [];
  ackClicked: boolean = false;
  serverScanComplete: boolean = false;
  dtOptions: DataTables.Settings = {};


  constructor(private http: HttpClient, private router: Router, private serverListService: ServerListService) {
  }

  ngOnInit() {
    this.getServerListSize();
    this.dtOptions = {
    };

    let timer2 = Observable.timer(5000, 2000);
    let timer = Observable.timer(60000, 60000);
    timer.subscribe(() => this.reload());
    timer2.subscribe(() => this.getServerListSize());
  }

  ngAfterViewInit(): void {
    this.serverListService.getServerList().subscribe(value => {
      this.serverList = value;
    });
  }

  getServerListSize() {
    this.http.get<number>("/api/management/server_list_size").subscribe((serverSize: number) => {
      this.serverSize = serverSize;
    });
  }

  acknowledgeServer(id) {
    this.http.get('/api/setup/server_acknowledge/' + id + "/").subscribe((success: boolean) => {
      if (success == true) {
        this.ackClicked = true;
        let timer = Observable.timer(5000);
        timer.subscribe(() => this.reload());
      }

    });
  }


  deleteServer(id) {
    this.http.get('/api/setup/server_delete/' + id + "/", {responseType: 'text'}).subscribe((success: any) => {
      console.log(success);
      this.reload();

    });
  }

  reload(): void {
    window.location.href = "/admin/servers";
  }

}
