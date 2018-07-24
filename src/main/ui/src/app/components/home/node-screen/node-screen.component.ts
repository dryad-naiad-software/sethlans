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

import {Component, OnInit} from '@angular/core';
import {NodeDashboard} from '../../../models/node_dash.model';
import {HttpClient} from '@angular/common/http';
import {timer} from 'rxjs/internal/observable/timer';
import {ComputeMethod} from '../../../enums/compute.method.enum';

@Component({
  selector: 'app-node-screen',
  templateUrl: './node-screen.component.html',
  styleUrls: ['./node-screen.component.scss']
})
export class NodeScreenComponent implements OnInit {
  nodeDash: NodeDashboard;
  method: any = ComputeMethod;


  constructor(private http: HttpClient) {
    this.nodeDash = new NodeDashboard();
  }

  ngOnInit() {
    this.getInfo();
    let scheduler = timer(5000, 5000);
    scheduler.subscribe(() => this.getInfo());
  }

  getInfo() {
    this.http.get('/api/info/node_dashboard').subscribe((nodeDash: NodeDashboard) => {
      this.nodeDash = nodeDash;
    });
  }

}
