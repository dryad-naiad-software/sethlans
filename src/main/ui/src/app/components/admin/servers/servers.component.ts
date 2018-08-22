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

import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {ServerInfo} from '../../../models/server_info.model';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {ServerListService} from '../../../services/server_list.service';
import {NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {timer} from 'rxjs';
import {AccessKeyListService} from '../../../services/access_key_list.service';
import {AccessKey} from '../../../models/access_key.model';
import {HttpParams} from '../../../../../node_modules/@angular/common/http';

@Component({
  selector: 'app-servers',
  templateUrl: './servers.component.html',
  styleUrls: ['./servers.component.scss']
})
export class ServersComponent implements OnInit {
  serverSize: number;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) accessKeyPaginator: MatPaginator;
  serverListDataSource = new MatTableDataSource();
  accessKeyListDataSource = new MatTableDataSource();
  accessKeyDisplayedColumns = ['accessKey', 'actions'];
  serverDisplayedColumns = ['serverStatus', 'hostname', 'ipAddress', 'port', 'actions'];
  selectedServer: ServerInfo;
  selectedKey: AccessKey;
  serverListToggle: boolean = true;
  keyToAdd: string;
  keyRejected: boolean = false;

  constructor(private http: HttpClient, private serverListService: ServerListService, private modalService: NgbModal, private accessKeyListService: AccessKeyListService) {
  }

  ngOnInit() {
    this.getInfo();
    let scheduler = timer(5000, 5000);
    scheduler.subscribe(() => this.getInfo());
  }

  addAccessKeyToNode() {

    let accessKeyObject = new HttpParams().set('access_key', this.keyToAdd);

    this.http.post('/api/setup/add_access_key', accessKeyObject, {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    }).subscribe((submitted: boolean) => {
      if (submitted === true) {
        this.keyRejected = false;
        this.loadAccessKeyTable();
        this.resetKey();
      }
      if (submitted === false) {
        this.keyRejected = true;
        this.loadAccessKeyTable();
        this.resetKey();
      }
    });
  }

  resetKey() {
    this.keyToAdd = '';
  }

  addAccessKeyModal(content) {
    this.keyToAdd = '';
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }

  getInfo() {
    this.serverListService.getServerListSize().subscribe(value => {
      if (this.serverSize != value) {
        this.loadServerTable();
      }
      this.serverSize = value;
    });
  }

  toggleList() {
    this.serverListToggle = !this.serverListToggle;
    if (this.serverListToggle == false) {
      this.loadAccessKeyTable();
    }
  }

  loadAccessKeyTable() {
    this.accessKeyListService.getAccessKeyList().subscribe(data => {
      this.accessKeyListDataSource = new MatTableDataSource<any>(data);
      this.accessKeyListDataSource.paginator = this.accessKeyPaginator;
    });
  }

  loadServerTable() {
    this.serverListService.getServerList().subscribe(data => {
      this.serverListDataSource = new MatTableDataSource<any>(data);
      this.serverListDataSource.paginator = this.paginator;
      this.serverListDataSource.sort = this.sort;
    });
  }

  deleteServer(id) {
    this.http.get('/api/setup/server_delete/' + id + '/').subscribe(() => {
      this.loadServerTable();
    });
  }

  deleteKey(id) {
    this.http.get('/api/setup/access_key_delete/' + id + '/').subscribe(() => {
      this.loadAccessKeyTable();
    });
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.serverListDataSource.filter = filterValue;
  }

  confirmServerDelete(server, content) {
    this.selectedServer = server;
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }

  confirmAccessKeyDelete(key, content) {
    this.selectedKey = key;
    let options: NgbModalOptions = {
      backdrop: 'static'
    };
    this.modalService.open(content, options);
  }


}
