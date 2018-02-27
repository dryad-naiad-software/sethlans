import {BrowserModule, Title} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AppComponent} from './app.component';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
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
import {FieldMatchesValidatorDirective} from "./directives/fieldmatchesvalidator.directive";
import {SetupServerComponent} from './components/setup-wizard/setup-server/setup-server.component';
import {SetupNodeComponent} from './components/setup-wizard/setup-node/setup-node.component';
import {SetupDualComponent} from './components/setup-wizard/setup-dual/setup-dual.component';
import {SetupSettingsComponent} from './components/setup-wizard/setup-settings/setup-settings.component';
import {MatSliderModule} from "@angular/material";
import {SetupSummaryComponent} from './components/setup-wizard/setup-summary/setup-summary.component';
import {SetupFinishedComponent} from './components/setup-wizard/setup-finished/setup-finished.component';
import {WindowRef} from "./services/windowref.service";
import {Ng2Webstorage} from "ngx-webstorage";
import {AuthService} from "./services/auth.service";
import {XhrInterceptor} from "./interceptor/XhrInterceptor";
import {RegisterUserComponent} from './components/register-user/register-user.component';

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
    FieldMatchesValidatorDirective,
    SetupServerComponent,
    SetupNodeComponent,
    SetupDualComponent,
    SetupSettingsComponent,
    SetupSummaryComponent,
    SetupFinishedComponent,
    RegisterUserComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    MatSliderModule,
    NgbModule.forRoot(),
    HttpClientModule,
    AppRoutingModule,
    Ng2Webstorage
  ],
  providers: [Title, SetupFormDataService, WindowRef, AuthService, {
    provide: HTTP_INTERCEPTORS,
    useClass: XhrInterceptor,
    multi: true
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}

