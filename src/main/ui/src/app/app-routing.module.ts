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

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './components/login/login.component';
import {HomeComponent} from './components/home/home.component';
import {ProjectsComponent} from './components/projects/projects.component';
import {GetStartedComponent} from './components/setup/get-started/get-started.component';
import {ShutdownComponent} from './components/admin/shutdown/shutdown.component';
import {RestartComponent} from './components/admin/restart/restart.component';
import {UserManagementComponent} from './components/admin/user-management/user-management.component';
import {UserAddComponent} from './components/admin/user-management/user-add/user-add.component';
import {UserEditComponent} from './components/admin/user-management/user-edit/user-edit.component';
import {SethlansSettingsComponent} from './components/admin/sethlans-settings/sethlans-settings.component';
import {ServersComponent} from './components/admin/servers/servers.component';
import {ComputeSettingsComponent} from './components/admin/compute-settings/compute-settings.component';
import {BlenderVersionsComponent} from './components/admin/blender-versions/blender-versions.component';
import {NodesComponent} from './components/admin/nodes/nodes.component';
import {LogsComponent} from './components/admin/logs/logs.component';
import {RegisterUserComponent} from './components/register-user/register-user.component';
import {UserSettingsComponent} from './components/user-settings/user-settings.component';
import {NodeWizardComponent} from './components/admin/nodes/node-wizard/node-wizard.component';

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'projects', component: ProjectsComponent},
  {path: 'get_started', component: GetStartedComponent},
  {path: 'shutdown', component: ShutdownComponent},
  {path: 'restart', component: RestartComponent},
  {path: 'admin/user_management', component: UserManagementComponent},
  {path: 'admin/user_management/add', component: UserAddComponent},
  {path: 'admin/user_management/edit/:id', component: UserEditComponent},
  {path: 'admin/sethlans_settings', component: SethlansSettingsComponent},
  {path: 'admin/servers', component: ServersComponent},
  {path: 'admin/compute_settings', component: ComputeSettingsComponent},
  {path: 'admin/blender_version_admin', component: BlenderVersionsComponent},
  {path: 'admin/nodes', component: NodesComponent},
  {path: 'admin/nodes/add', component: NodeWizardComponent},
  {path: 'admin/logs', component: LogsComponent},
  {path: 'register', component: RegisterUserComponent},
  {path: 'user_settings', component: UserSettingsComponent},
];

@NgModule({
  exports: [RouterModule],
  imports: [
    RouterModule.forRoot(routes)
  ]
})
export class AppRoutingModule {
}
