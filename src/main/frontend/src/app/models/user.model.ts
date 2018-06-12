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

import {Role} from '../enums/role.enum';

export class User {
  username: string;
  password: string;
  passwordConfirm: string;
  email: string;
  roles: Role[];
  active: boolean;
  passwordUpdated: boolean;


  setUserName(username: string) {
    this.username = username;
  }

  getUserName(): string {
    return this.username;
  }

  getPassword(): string {
    return this.password;
  }

  setPassword(password: string) {
    this.password = password;
  }

  setEmail(email: string) {
    this.email = email;
  }

  getEmail(): string {
    return this.email;
  }

  setActive(active: boolean) {
    this.active = active;
  }

  isActive(): boolean {
    return this.active;
  }

  setPasswordConfirm(passwordConfirm: string) {
    this.passwordConfirm = passwordConfirm;

  }

  getRoles(): Role[] {
    return this.roles;
  }

  setRoles(roles: Role[]) {
    this.roles = roles;
  }

  isPasswordUpdated(): boolean {
    return this.passwordUpdated;
  }

  setPasswordUpdated(passwordUpdated: boolean) {
    this.passwordUpdated = passwordUpdated;
  }
}
