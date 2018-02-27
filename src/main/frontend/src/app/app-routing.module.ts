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
import {SettingsComponent} from "./components/settings/settings.component";
import {RegisterUserComponent} from "./components/register-user/register-user.component";

;

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'settings', component: SettingsComponent},
  {path: 'register', component: RegisterUserComponent},
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
