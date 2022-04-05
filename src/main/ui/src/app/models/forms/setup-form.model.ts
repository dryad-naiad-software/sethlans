/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {Mode} from "../../enums/mode.enum";
import {User} from "../user/user.model";
import {LogLevel} from "../../enums/loglevel.enum";
import {NodeType} from "../../enums/nodetype.enum";
import {GPU} from "../hardware/gpu.model";
import {MailSettings} from "../settings/mailsettings.model";
import {NodeSettings} from "../settings/nodesettings.model";
import {ServerSettings} from "../settings/serversettings.model";
import {SystemInfo} from "../system/systeminfo.model";

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class SetupForm {
  mode: Mode;
  user: User;
  logLevel: LogLevel;
  availableTypes: Array<NodeType>;
  availableGPUs: Array<GPU>;
  blenderVersions: Array<string>;
  mailSettings: MailSettings;
  nodeSettings: NodeSettings;
  serverSettings: ServerSettings;
  systemInfo: SystemInfo;
  ipAddress: string;
  port: string;
  appURL: string;


  constructor(obj: any) {
    this.mode = obj.mode;
    this.user = obj.user;
    this.logLevel = obj.logLevel;
    this.availableTypes = obj.availableTypes;
    this.availableGPUs = obj.availableGPUs;
    this.blenderVersions = obj.blenderVersions;
    this.mailSettings = obj.mailSettings;
    this.nodeSettings = obj.nodeSettings;
    this.serverSettings = obj.serverSettings;
    this.systemInfo = obj.systemInfo;
    this.ipAddress = obj.ipAddress;
    this.port = obj.port;
    this.appURL = obj.appURL;
  }
}
