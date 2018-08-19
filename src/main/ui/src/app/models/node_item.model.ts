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

export class NodeItem {
  ipAddress: string;
  port: string;
  active: boolean;

  constructor() {
    this.ipAddress = '';
    this.port = '';
    this.active = false;

  }

  nodeItemNotReady(): boolean {
    return this.ipAddress == null || this.port == null || this.ipAddress.length === 0 || this.port.length === 0;
  }
}