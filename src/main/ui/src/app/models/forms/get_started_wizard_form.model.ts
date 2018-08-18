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

import {GetStartedProgress} from '../../enums/get_started_progress.enum';
import {NodeItem} from '../node_item.model';
import {Login} from '../login.model';
import {NodeInfo} from '../node_info.model';

export class GetStartedWizardForm {
  currentProgress: GetStartedProgress;
  listOfNodes: NodeItem[];
  nodeLogin: Login;
  nodesToAdd: NodeInfo[];
  scanComplete: boolean;


  constructor() {
    this.currentProgress = GetStartedProgress.START;
    this.listOfNodes = [];
    this.nodesToAdd = [];
    this.nodeLogin = new Login();
    this.scanComplete = false;
  }

}
