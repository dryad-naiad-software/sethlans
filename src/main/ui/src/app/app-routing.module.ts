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
import {RouterModule, Routes} from '@angular/router';
import {DashboardComponent} from './dashboard/dashboard.component';
import {LoginComponent} from "./login/login.component";
import {NodesComponent} from "./admin/nodes/nodes.component";
import {AuthorizedServerComponent} from "./admin/authorized-server/authorized-server.component";
import {ComputeSettingsComponent} from "./admin/compute-settings/compute-settings.component";
import {ProjectsComponent} from "./projects/projects.component";


const routes: Routes = [
  {path: '', component: DashboardComponent},
  {path: 'login', component: LoginComponent},
  {path: 'admin/nodes', component: NodesComponent},
  {path: 'admin/server', component: AuthorizedServerComponent},
  {path: 'admin/compute_settings', component: ComputeSettingsComponent},
  {path: 'projects', component: ProjectsComponent}

];


@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
