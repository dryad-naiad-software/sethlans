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

import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {NavBarComponent} from './layout/nav-bar/nav-bar.component';
import {SetupWizardComponent} from './setup-wizard/setup-wizard.component';
import {HttpClientModule} from '@angular/common/http';
import {AppRoutingModule} from './app-routing.module';
import {DashboardComponent} from './dashboard/dashboard.component';
import {FooterComponent} from './layout/footer/footer.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {FormsModule} from "@angular/forms";
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpErrorHandler} from "./services/http-error-handler.service";
import {MessageService} from "./services/message.service";
import {LoginComponent} from './login/login.component';
import {ServerDashComponent} from './dashboard/server-dash/server-dash.component';
import {NodeDashComponent} from './dashboard/node-dash/node-dash.component';
import {NodesComponent} from './admin/nodes/nodes.component';
import {AuthorizedServerComponent} from './admin/authorized-server/authorized-server.component';
import {ComputeSettingsComponent} from './admin/compute-settings/compute-settings.component';
import {ProjectsComponent} from './projects/projects.component';
import {FileUploadModule} from "primeng/fileupload";
import {CdkTableModule} from "@angular/cdk/table";

/**
 * File created by Mario Estrella on 4/3/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */

@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    SetupWizardComponent,
    DashboardComponent,
    FooterComponent,
    LoginComponent,
    ServerDashComponent,
    NodeDashComponent,
    NodesComponent,
    AuthorizedServerComponent,
    ComputeSettingsComponent,
    ProjectsComponent],
  imports: [
    BrowserModule, HttpClientModule, AppRoutingModule, NgbModule, CdkTableModule,
    FontAwesomeModule, FormsModule, BrowserAnimationsModule, FileUploadModule,
  ],
  providers: [HttpErrorHandler, MessageService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
