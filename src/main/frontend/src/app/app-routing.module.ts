import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./components/home/home.component";
import {SetupWizardComponent} from "./components/setup-wizard/setup-wizard.component";

const routes: Routes = [
  {path: 'setup', component: SetupWizardComponent},
  {path: '', component: HomeComponent}


];

@NgModule({
  exports: [RouterModule],
  imports: [
    RouterModule.forRoot(routes)
  ]
})
export class AppRoutingModule {
}
