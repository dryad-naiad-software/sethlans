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

import {NodeWizardMode} from '../../enums/node_wizard_mode.enum';
import {NodeAddType} from '../../enums/node_wizard_add_type.enum';
import {NodeItem} from '../node_item.model';
import {NodeInfo} from '../node_info.model';

export class NodeWizardForm {
  currentMode: NodeWizardMode;
  addType: NodeAddType;
  multipleNodeAdd: boolean;
  multipleNodes: NodeItem[];
  singleNode: NodeItem;
  finished: boolean;
  summaryComplete: boolean;
  nodeToAdd: NodeInfo;
  nodesToAdd: NodeInfo[];


  constructor() {
    this.currentMode = NodeWizardMode.Start;
    this.finished = false;
    this.summaryComplete = false;
    this.multipleNodeAdd = false;
  }
}
