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

import {Component, Input, OnInit} from '@angular/core';
import {NodeItem} from '../../../../../models/node_item.model';

@Component({
  selector: 'app-node-summary',
  templateUrl: './node-summary.component.html',
  styleUrls: ['./node-summary.component.scss']
})
export class NodeSummaryComponent implements OnInit {
  @Input() summaryComplete: boolean;
  @Input() multipleNodes: NodeItem[];
  @Input() singleNode: NodeItem;
  @Input() multipleNodeAdd: boolean;


  constructor() {
  }

  ngOnInit() {
  }

}
