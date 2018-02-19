import {BrowserModule, Title} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';


import {AppComponent} from './app.component';
import {HttpClientModule} from "@angular/common/http";
import {NavbarComponent} from './components/navbar/navbar.component';
import {LoginComponent} from './components/login/login.component';
import {SettingsComponent} from './components/settings/settings.component';
import {NotFoundComponent} from './components/not-found/not-found.component';
import {FooterComponent} from './components/footer/footer.component';
import {AppRoutingModule} from './app-routing.module';
import {HomeComponent} from './components/home/home.component';
import {SetupWizardComponent} from './components/setup-wizard/setup-wizard.component';
import {SetupModeComponent} from './components/setup-wizard/setup-mode/setup-mode.component';
import {SetupRegisterUserComponent} from './components/setup-wizard/setup-register-user/setup-register-user.component';
import {FormsModule} from "@angular/forms";
import {SetupFormDataService} from "./services/setupformdata.service";


@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    LoginComponent,
    SettingsComponent,
    NotFoundComponent,
    FooterComponent,
    HomeComponent,
    SetupWizardComponent,
    SetupModeComponent,
    SetupRegisterUserComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule,
    NgbModule.forRoot(),
    HttpClientModule,
    AppRoutingModule
  ],
  providers: [Title, SetupFormDataService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
