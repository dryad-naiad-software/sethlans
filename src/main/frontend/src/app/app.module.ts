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

import {BrowserModule, Title} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AppComponent} from './app.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {NavbarComponent} from './components/layouts/navbar/navbar.component';
import {LoginComponent} from './components/login/login.component';
import {NotFoundComponent} from './components/not-found/not-found.component';
import {FooterComponent} from './components/layouts/footer/footer.component';
import {AppRoutingModule} from './app-routing.module';
import {HomeComponent} from './components/home/home.component';
import {SetupWizardComponent} from './components/setup-wizard/setup-wizard.component';
import {SetupModeComponent} from './components/setup-wizard/setup-mode/setup-mode.component';
import {SetupRegisterUserComponent} from './components/setup-wizard/setup-register-user/setup-register-user.component';
import {FormsModule} from "@angular/forms";
import {SetupFormDataService} from "./services/setupformdata.service";
import {FieldMatchesValidatorDirective} from "./directives/fieldmatchesvalidator.directive";
import {SetupServerComponent} from './components/setup-wizard/setup-server/setup-server.component';
import {SetupNodeComponent} from './components/setup-wizard/setup-node/setup-node.component';
import {SetupDualComponent} from './components/setup-wizard/setup-dual/setup-dual.component';
import {SetupSettingsComponent} from './components/setup-wizard/setup-settings/setup-settings.component';
import {MatSliderModule} from "@angular/material";
import {SetupSummaryComponent} from './components/setup-wizard/setup-summary/setup-summary.component';
import {SetupFinishedComponent} from './components/setup-wizard/setup-finished/setup-finished.component';
import {WindowRef} from "./services/windowref.service";
import {Ng2Webstorage} from "ngx-webstorage";
import {AuthService} from "./services/auth.service";
import {XhrInterceptor} from "./interceptor/XhrInterceptor";
import {RegisterUserComponent} from './components/register-user/register-user.component';
import {UserSettingsComponent} from './components/user-settings/user-settings.component';
import {MetricsComponent} from './components/admin/metrics/metrics.component';
import {SethlansSettingsComponent} from './components/admin/sethlans-settings/sethlans-settings.component';
import {LogsComponent} from './components/admin/logs/logs.component';
import {UserManagementComponent} from './components/admin/user-management/user-management.component';
import {MetricsService} from "./services/metrics.service";
import {UserAddEditComponent} from './components/admin/user-management/user-add-edit/user-add-edit.component';
import {UserViewComponent} from './components/admin/user-management/user-view/user-view.component';
import {ComputeSettingsComponent} from './components/admin/compute-settings/compute-settings.component';
import {BlenderVersionsComponent} from './components/admin/blender-versions/blender-versions.component';
import {ProjectsComponent} from './components/projects/projects.component';
import {NodesComponent} from './components/admin/nodes/nodes.component';
import {ServersComponent} from './components/admin/servers/servers.component';
import {HelpComponent} from './components/help/help.component';
import {NodeScreenComponent} from './components/home/node-screen/node-screen.component';
import {ServerScreenComponent} from './components/home/server-screen/server-screen.component';
import {DataTablesModule} from "angular-datatables";
import {FileUploadModule} from 'primeng/primeng';
import {KeysPipe} from "./pipes/keys.pipe";
import {ProjectModalComponent} from './components/projects/project-modal/project-modal.component';
import {ProjectDetailsComponent} from './components/projects/project-details/project-details.component';


@NgModule({
  declarations: [
    AppComponent,
    KeysPipe,
    NavbarComponent,
    LoginComponent,
    NotFoundComponent,
    FooterComponent,
    HomeComponent,
    SetupWizardComponent,
    SetupModeComponent,
    SetupRegisterUserComponent,
    FieldMatchesValidatorDirective,
    SetupServerComponent,
    SetupNodeComponent,
    SetupDualComponent,
    SetupSettingsComponent,
    SetupSummaryComponent,
    SetupFinishedComponent,
    RegisterUserComponent,
    UserSettingsComponent,
    MetricsComponent,
    SethlansSettingsComponent,
    LogsComponent,
    UserManagementComponent,
    UserAddEditComponent,
    UserViewComponent,
    ComputeSettingsComponent,
    BlenderVersionsComponent,
    ProjectsComponent,
    NodesComponent,
    ServersComponent,
    HelpComponent,
    NodeScreenComponent,
    ServerScreenComponent,
    ProjectModalComponent,
    ProjectDetailsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    FileUploadModule,
    MatSliderModule,
    DataTablesModule,
    NgbModule.forRoot(),
    HttpClientModule,
    AppRoutingModule,
    Ng2Webstorage
  ],
  providers: [Title, SetupFormDataService, WindowRef, AuthService, MetricsService, {
    provide: HTTP_INTERCEPTORS,
    useClass: XhrInterceptor,
    multi: true
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}

