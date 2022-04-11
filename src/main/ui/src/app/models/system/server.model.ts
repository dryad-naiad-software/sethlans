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

/**
 * File created by Mario Estrella on 4/9/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class Server {
  hostname: string;
  ipAddress: string;
  networkPort: string;
  systemID: string;
  benchmarkComplete: boolean;

  constructor() {
    this.hostname = '';
    this.ipAddress = '';
    this.networkPort = '';
    this.systemID = '';
    this.benchmarkComplete = false;
  }

  setServer(obj: any) {
    this.hostname = obj.hostname;
    this.ipAddress = obj.ipAddress;
    this.networkPort = obj.networkPort;
    this.systemID = obj.systemID;
    this.benchmarkComplete = obj.benchmarkComplete;

  }
}
