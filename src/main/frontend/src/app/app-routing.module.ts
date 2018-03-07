import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./components/home/home.component";
import {SetupWizardComponent} from "./components/setup-wizard/setup-wizard.component";
import {SetupModeComponent} from "./components/setup-wizard/setup-mode/setup-mode.component";
import {SetupRegisterUserComponent} from "./components/setup-wizard/setup-register-user/setup-register-user.component";
import {SetupServerComponent} from "./components/setup-wizard/setup-server/setup-server.component";
import {SetupNodeComponent} from "./components/setup-wizard/setup-node/setup-node.component";
import {SetupDualComponent} from "./components/setup-wizard/setup-dual/setup-dual.component";
import {SetupSettingsComponent} from "./components/setup-wizard/setup-settings/setup-settings.component";
import {SetupSummaryComponent} from "./components/setup-wizard/setup-summary/setup-summary.component";
import {SetupFinishedComponent} from "./components/setup-wizard/setup-finished/setup-finished.component";
import {LoginComponent} from "./components/login/login.component";
import {RegisterUserComponent} from "./components/register-user/register-user.component";
import {UserSettingsComponent} from "./components/user-settings/user-settings.component";
import {MetricsComponent} from "./components/admin/metrics/metrics.component";
import {UserManagementComponent} from "./components/admin/user-management/user-management.component";
import {SethlansSettingsComponent} from "./components/admin/sethlans-settings/sethlans-settings.component";
import {ComputeSettingsComponent} from "./components/admin/compute-settings/compute-settings.component";
import {BlenderVersionsComponent} from "./components/admin/blender-versions/blender-versions.component";
import {LogsComponent} from "./components/admin/logs/logs.component";
import {ProjectsComponent} from "./components/projects/projects.component";
import {ServersComponent} from "./components/admin/servers/servers.component";
import {NodesComponent} from "./components/admin/nodes/nodes.component";
import {HelpComponent} from "./components/help/help.component";


const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'help', component: HelpComponent},
  {path: 'admin/metrics', component: MetricsComponent},
  {path: 'admin/user_management', component: UserManagementComponent},
  {path: 'admin/sethlans_settings', component: SethlansSettingsComponent},
  {path: 'admin/servers', component: ServersComponent},
  {path: 'admin/compute_settings', component: ComputeSettingsComponent},
  {path: 'admin/blender_version_admin', component: BlenderVersionsComponent},
  {path: 'admin/nodes', component: NodesComponent},
  {path: 'admin/logs', component: LogsComponent},
  {path: 'register', component: RegisterUserComponent},
  {path: 'user_settings', component: UserSettingsComponent},
  {path: 'projects', component: ProjectsComponent},
  {
    path: '', component: SetupWizardComponent, outlet: 'setup', children: [
      {path: '', component: SetupModeComponent, outlet: 'mode'},
      {path: '', component: SetupRegisterUserComponent, outlet: 'user_register'},
      {path: '', component: SetupServerComponent, outlet: 'server'},
      {path: '', component: SetupNodeComponent, outlet: 'node'},
      {path: '', component: SetupDualComponent, outlet: 'dual'},
      {path: '', component: SetupSettingsComponent, outlet: 'settings'},
      {path: '', component: SetupSummaryComponent, outlet: 'summary'},
      {path: '', component: SetupFinishedComponent, outlet: 'finished'}
    ]
  }


];

@NgModule({
  exports: [RouterModule],
  imports: [
    RouterModule.forRoot(routes)
  ]
})
export class AppRoutingModule {
}
