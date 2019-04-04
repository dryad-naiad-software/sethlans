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

import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {HttpClient} from '@angular/common/http';
import {RenderTaskHistory} from '../../../models/render_task_history.model';

@Component({
  selector: 'app-render-history',
  templateUrl: './node-render-history.component.html',
  styleUrls: ['./node-render-history.component.scss']
})
export class NodeRenderHistoryComponent implements OnInit {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['taskDate', 'computeType', 'deviceIDs', 'completed', 'failed', 'engine', 'projectName', 'frameAndPartNumbers', 'serverName'];

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.http.get('/api/management/render_history_list').subscribe((renderHistoryList: RenderTaskHistory[]) => {
      this.dataSource = new MatTableDataSource<any>(renderHistoryList.reverse());
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
    });
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }
}
