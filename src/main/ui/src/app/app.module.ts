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
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {
  MatCheckboxModule,
  MatFormFieldModule,
  MatInputModule,
  MatPaginatorModule,
  MatSliderModule,
  MatSortModule,
  MatTableModule
} from '@angular/material';

import {AppComponent} from './app.component';
import {NavBarComponent} from './components/layouts/nav-bar/nav-bar.component';
import {FooterComponent} from './components/layouts/footer/footer.component';
import {SetupWizardComponent} from './components/setup/setup-wizard/setup-wizard.component';
import {GetStartedComponent} from './components/setup/get-started/get-started.component';
import {ModeSetupComponent} from './components/setup/setup-wizard/mode-setup/mode-setup.component';
import {DualConfigComponent} from './components/setup/setup-wizard/dual-config/dual-config.component';
import {NodeConfigComponent} from './components/setup/setup-wizard/node-config/node-config.component';
import {ServerConfigComponent} from './components/setup/setup-wizard/server-config/server-config.component';
import {UserCreateComponent} from './components/setup/setup-wizard/user-create/user-create.component';
import {SettingsConfigComponent} from './components/setup/setup-wizard/settings-config/settings-config.component';
import {SetupSummaryComponent} from './components/setup/setup-wizard/setup-summary/setup-summary.component';
import {SetupFinishedComponent} from './components/setup/setup-wizard/setup-finished/setup-finished.component';
import {KeysPipe} from './pipes/keys.pipe';
import {FieldmatchesvalidatorDirective} from './directives/fieldmatchesvalidator.directive';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {WindowRef} from './services/windowref.service';
import {LoginService} from './services/login.service';
import {XhrInterceptor} from './services/xhrinterceptor';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {AppRoutingModule} from './app-routing.module';
import {ProjectsComponent} from './components/projects/projects.component';
import {RegisterUserComponent} from './components/register-user/register-user.component';
import {NodeScreenComponent} from './components/home/node-screen/node-screen.component';
import {ServerScreenComponent} from './components/home/server-screen/server-screen.component';
import {ProjectListService} from './services/project_list.service';
import {ChartModule} from 'primeng/chart';
import {ShutdownComponent} from './components/admin/shutdown/shutdown.component';
import {RestartComponent} from './components/admin/restart/restart.component';
import {UserManagementComponent} from './components/admin/user-management/user-management.component';
import {UserEditComponent} from './components/admin/user-management/user-edit/user-edit.component';
import {UserAddComponent} from './components/admin/user-management/user-add/user-add.component';
import {ServersComponent} from './components/admin/servers/servers.component';
import {NodesComponent} from './components/admin/nodes/nodes.component';
import {UserSettingsComponent} from './components/user-settings/user-settings.component';
import {SethlansSettingsComponent} from './components/admin/sethlans-settings/sethlans-settings.component';
import {ComputeSettingsComponent} from './components/admin/compute-settings/compute-settings.component';
import {BlenderVersionsComponent} from './components/admin/blender-versions/blender-versions.component';
import {LogsComponent} from './components/admin/logs/logs.component';
import {UserListService} from './services/user_list.service';
import {NodeListService} from './services/node_list.service';
import {NodeAddComponent} from './components/admin/nodes/node-add/node-add.component';
import {ServerListService} from './services/server_list.service';
import {AccessKeyListService} from './services/access_key_list.service';
import {NodeWizardComponent} from './components/admin/nodes/node-wizard/node-wizard.component';
import {NodeManualAddComponent} from './components/admin/nodes/node-wizard/node-manual-add/node-manual-add.component';
import {NodeScanAddComponent} from './components/admin/nodes/node-wizard/node-scan-add/node-scan-add.component';

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
    SetupFinishedComponent,
    KeysPipe,
    FieldmatchesvalidatorDirective,
    HomeComponent,
    LoginComponent,
    ProjectsComponent,
    RegisterUserComponent,
    NodeScreenComponent,
    ServerScreenComponent,
    ShutdownComponent,
    RestartComponent,
    UserManagementComponent,
    UserEditComponent,
    UserAddComponent,
    ServersComponent,
    NodesComponent,
    UserSettingsComponent,
    SethlansSettingsComponent,
    ComputeSettingsComponent,
    BlenderVersionsComponent,
    LogsComponent,
    NodeAddComponent,
    NodeWizardComponent,
    NodeManualAddComponent,
    NodeScanAddComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ChartModule,
    MatSliderModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatInputModule,
    MatSortModule,
    HttpClientModule,
    FormsModule,
    NgbModule.forRoot(),
    AppRoutingModule
  ],
  providers: [WindowRef, LoginService, ProjectListService, UserListService, NodeListService, ServerListService, AccessKeyListService, {
    provide: HTTP_INTERCEPTORS,
    useClass: XhrInterceptor,
    multi: true
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}
