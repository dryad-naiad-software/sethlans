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

import {Mode} from '../../enums/mode.enum';
import {User} from '../user.model';
import {Server} from '../server.model';
import {SethlansNode} from '../sethlan_node.model';
import {MailSettings} from '../mail_settings_model';

export class SetupWizardForm {
  mode: Mode;
  user: User;
  server: Server;
  mailSettings: MailSettings;
  node: SethlansNode;
  ipAddress: string;
  port: number;
  appURL: string;
  rootDirectory: string;
  complete: boolean;
  logLevel: string;
  isModeDone: boolean;
  configureMail: boolean;
  mailSettingsComplete: boolean;
  showMailSettings: boolean = false;


  constructor() {
    this.mode = Mode.SERVER;
    this.complete = false;
    this.logLevel = 'DEBUG'; // TODO: Change to INFO for releases.  DEBUG for beta/testing.
    this.isModeDone = false;
    this.mailSettings = new MailSettings();
    this.mailSettingsComplete = false;
    this.configureMail = false;
  }
}
