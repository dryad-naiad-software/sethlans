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
 * File created by Mario Estrella on 4/4/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

export class MailSettings {
  mailHost: string;
  mailPort: string;
  username: string;
  password: string;
  replyToAddress: string;
  mailEnabled: boolean;
  smtpAuth: boolean;
  sslEnabled: boolean;
  startTLSEnabled: boolean;
  startTLSRequired: boolean;

  constructor(obj: any) {
    this.mailHost = obj.mailHost;
    this.mailPort = obj.mailPort;
    this.username = obj.username;
    this.password = obj.password;
    this.replyToAddress = obj.replyToAddress;
    this.mailEnabled = obj.mailEnabled;
    this.smtpAuth = obj.smtpAuth;
    this.sslEnabled = obj.sslEnabled;
    this.startTLSEnabled = obj.startTLSEnabled;
    this.startTLSRequired = obj.startTLSRequired;
  }
}