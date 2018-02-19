import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./components/home/home.component";
import {SetupWizardComponent} from "./components/setup-wizard/setup-wizard.component";
import {SetupModeComponent} from "./components/setup-wizard/setup-mode/setup-mode.component";
import {SetupRegisterUserComponent} from "./components/setup-wizard/setup-register-user/setup-register-user.component";

;

const routes: Routes = [
  {path: '', component: HomeComponent, outlet: 'home'},
  {
    path: '', component: SetupWizardComponent, outlet: 'setup', children: [
      {path: '', component: SetupModeComponent, outlet: 'mode'},
      {path: '', component: SetupRegisterUserComponent, outlet: 'user_register'}
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
