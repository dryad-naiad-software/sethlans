/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import {Mode} from '../enums/mode.enum';
import {SetupProgress} from '../enums/setupProgress.enum';
import {User} from './user.model';
import {Server} from './server.model';
import {Node} from './node.model';

export class SetupFormData {
  private mode: Mode;
  private setupProgress: SetupProgress;
  private user: User;
  private server: Server;
  private node: Node;
  private ipAddress: string;
  private port: number;
  private rootDirectory: string;

  constructor() {
    this.setupProgress = SetupProgress.START;
    this.mode = Mode.SERVER;
    this.user = new User();
    this.server = null;
    this.node = null;
  }

  setMode(mode: Mode) {
    this.mode = mode;
  }

  getMode(): Mode {
    return this.mode;
  }

  setProgress(setupProgress: SetupProgress) {
    this.setupProgress = setupProgress;
  }

  getProgress(): SetupProgress {
    return this.setupProgress;
  }

  setUser(user: User) {
    this.user = user;
  }

  getUser(): User {
    return this.user;
  }

  setServer(server: Server) {
    this.server = server;
  }

  getServer(): Server {
    return this.server;
  }

  setNode(node: Node) {
    this.node = node;
  }

  getNode(): Node {
    return this.node;
  }

  setIPAddress(ipAddress: string) {
    this.ipAddress = ipAddress;
  }

  getIPAddress(): string {
    return this.ipAddress;
  }

  setSethlansPort(port: number) {
    this.port = port;
  }

  getSethlansPort(): number {
    return this.port;
  }

  setRootDirectory(rootDirectory: string) {
    this.rootDirectory = rootDirectory;
  }

  getRootDirectory(): string {
    return this.rootDirectory;
  }

}
