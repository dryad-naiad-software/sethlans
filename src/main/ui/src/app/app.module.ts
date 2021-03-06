/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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
  MatGridListModule,
  MatInputModule,
  MatPaginatorModule,
  MatProgressBarModule,
  MatSliderModule,
  MatSortModule,
  MatTableModule
} from '@angular/material';

import {AppComponent} from './app.component';
import {NavBarComponent} from './components/layouts/nav-bar/nav-bar.component';
import {FooterComponent} from './components/layouts/footer/footer.component';
import {SetupWizardComponent} from './components/setup/setup-wizard/setup-wizard.component';
import {GetStartedWizardComponent} from './components/setup/get-started-wizard/get-started-wizard.component';
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
import {ServerListService} from './services/server_list.service';
import {AccessKeyListService} from './services/access_key_list.service';
import {NodeWizardComponent} from './components/admin/nodes/node-wizard/node-wizard.component';
import {NodeManualAddComponent} from './components/admin/nodes/node-wizard/node-manual-add/node-manual-add.component';
import {NodeScanAddComponent} from './components/admin/nodes/node-wizard/node-scan-add/node-scan-add.component';
import {NodeSummaryComponent} from './components/admin/nodes/node-wizard/node-summary/node-summary.component';
import {ProjectWizardComponent} from './components/projects/project-create-wizard/project-wizard.component';
import {ProjectUploadComponent} from './components/projects/project-create-wizard/project-upload/project-upload.component';
import {ProjectDetailsComponent} from './components/projects/project-create-wizard/project-details/project-details.component';
import {ProjectRenderSettingsComponent} from './components/projects/project-create-wizard/project-render-settings/project-render-settings.component';
import {ProjectSummaryComponent} from './components/projects/project-create-wizard/project-summary/project-summary.component';
import {FileUploadModule} from 'primeng/fileupload';
import {ProjectViewComponent} from './components/projects/project-view/project-view.component';
import {ProjectEditWizardComponent} from './components/projects/project-edit-wizard/project-edit-wizard.component';
import {ProjectEditVideoSettingsComponent} from './components/projects/project-edit-video-settings/project-edit-video-settings.component';
import {WizardNodeAuthComponent} from './components/setup/get-started-wizard/wizard-node-auth/wizard-node-auth.component';
import {WizardAddNodesComponent} from './components/setup/get-started-wizard/wizard-add-nodes/wizard-add-nodes.component';
import {ForgotPassComponent} from './components/forgot-pass/forgot-pass.component';
import {NodeRenderHistoryComponent} from './components/admin/node-render-history/node-render-history.component';
import {InfiniteScrollModule} from 'ngx-infinite-scroll';
import {ProjectFramesComponent} from './components/projects/project-frames/project-frames.component';
import {NodeAuthComponent} from './components/admin/nodes/node-wizard/node-auth/node-auth.component';
import {ServerQueueHistoryComponent} from './components/admin/server-queue-history/server-queue-history.component';
import {ProjectQueueListComponent} from './components/projects/project-queue-list/project-queue-list.component';
import {ProjectVideoSettingsComponent} from './components/projects/project-create-wizard/project-video-settings/project-video-settings.component';


@NgModule({
  declarations: [
    AppComponent,
    NavBarComponent,
    FooterComponent,
    SetupWizardComponent,
    GetStartedWizardComponent,
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
    NodeWizardComponent,
    NodeManualAddComponent,
    NodeScanAddComponent,
    NodeSummaryComponent,
    ProjectWizardComponent,
    ProjectUploadComponent,
    ProjectDetailsComponent,
    ProjectRenderSettingsComponent,
    ProjectSummaryComponent,
    ProjectViewComponent,
    ProjectEditWizardComponent,
    ProjectEditVideoSettingsComponent,
    WizardNodeAuthComponent,
    WizardAddNodesComponent,
    ForgotPassComponent,
    NodeRenderHistoryComponent,
    ProjectFramesComponent,
    NodeAuthComponent,
    ServerQueueHistoryComponent,
    ProjectQueueListComponent,
    ProjectVideoSettingsComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    ChartModule,
    MatSliderModule,
    MatTableModule,
    MatGridListModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatInputModule,
    MatSortModule,
    HttpClientModule,
    MatProgressBarModule,
    FormsModule,
    FileUploadModule,
    NgbModule,
    AppRoutingModule,
    InfiniteScrollModule,
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
