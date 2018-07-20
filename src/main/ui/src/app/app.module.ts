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

import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';


import {AppComponent} from './app.component';
import {NavBarComponent} from './compontents/layouts/nav-bar/nav-bar.component';
import {FooterComponent} from './compontents/layouts/footer/footer.component';
import {SetupWizardComponent} from './compontents/setup/setup-wizard/setup-wizard.component';
import {GetStartedComponent} from './compontents/setup/get-started/get-started.component';
import {ModeSetupComponent} from './compontents/setup/setup-wizard/mode-setup/mode-setup.component';
import {DualConfigComponent} from './compontents/setup/setup-wizard/dual-config/dual-config.component';
import {NodeConfigComponent} from './compontents/setup/setup-wizard/node-config/node-config.component';
import {ServerConfigComponent} from './compontents/setup/setup-wizard/server-config/server-config.component';
import {UserCreateComponent} from './compontents/setup/setup-wizard/user-create/user-create.component';
import {SettingsConfigComponent} from './compontents/setup/setup-wizard/settings-config/settings-config.component';
import {SetupSummaryComponent} from './compontents/setup/setup-wizard/setup-summary/setup-summary.component';
import {SetupFinishedComponent} from './compontents/setup/setup-wizard/setup-finished/setup-finished.component';

@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    FooterComponent,
    SetupWizardComponent,
    GetStartedComponent,
    ModeSetupComponent,
    DualConfigComponent,
    NodeConfigComponent,
    ServerConfigComponent,
    UserCreateComponent,
    SettingsConfigComponent,
    SetupSummaryComponent,
    SetupFinishedComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    NgbModule.forRoot()
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
