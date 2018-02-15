import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';


import {AppComponent} from './app.component';
import {HttpClientModule} from "@angular/common/http";
import {NavbarComponent} from './components/navbar/navbar.component';
import {LoginComponent} from './components/login/login.component';
import {SettingsComponent} from './components/settings/settings.component';
import {NotFoundComponent} from './components/not-found/not-found.component';
import {SetupWizardComponent} from './components/setup-wizard/setup-wizard.component';
import {FooterComponent} from './components/footer/footer.component';


@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    LoginComponent,
    SettingsComponent,
    NotFoundComponent,
    SetupWizardComponent,
    FooterComponent
  ],
  imports: [
    BrowserModule,
    NgbModule.forRoot(),
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
