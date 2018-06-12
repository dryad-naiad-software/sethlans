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
port
{
  Component, OnInit, ViewChild
}
from
'@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/timer";
import {ServerListService} from "../../../services/server_list.service";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {ServerInfo} from "../../../models/server_info.model";
import {NgbModal, NgbModalOptions} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-servers',
  templateUrl: './servers.component.html',
  styleUrls: ['./servers.component.scss']
})
export class ServersComponent implements OnInit {
  serverSize: number;
  ackClicked: boolean = false;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['serverStatus', 'hostname', 'ipAddress', 'port', 'actions'];
  selectedServer: ServerInfo;


  constructor(private http: HttpClient, private router: Router, private serverListService: ServerListService, private modalService: NgbModal) {
  }

  ngOnInit() {
    this.getInfo();

    let timer = Observable.timer(5000, 5000);
    timer.subscribe(() => this.getInfo());
  }

  getInfo() {
    this.serverListService.getServerListSize().subscribe(value => {
      if (this.serverSize != value) {
        this.loadTable();
      }
      this.serverSize = value;
    })
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }

  confirm(server, content) {
    this.selectedServer = server;
    let options: NgbModalOptions = {
      backdrop: "static"
    };
    this.modalService.open(content, options);
  }


  loadTable() {
    this.serverListService.getServerList().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  acknowledgeServer(id) {
    this.http.get('/api/setup/server_acknowledge/' + id + "/").subscribe(() => {
        this.ackClicked = true;
      setTimeout(() => {
        window.location.href = "/admin/servers";
      }, 1000);
    });
  }


  deleteServer(id) {
    this.http.get('/api/setup/server_delete/' + id + "/").subscribe(() => {
      this.loadTable();
    });
  }


}
