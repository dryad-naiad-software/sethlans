import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Login} from "../../models/login.model";
import {AuthService} from "../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  login: Login;

  constructor(private http: HttpClient, private authService: AuthService, private router: Router) {
  }

  ngOnInit() {
    this.login = new Login();
  }

  onSubmit() {
    this.authService.login(this.login);
  }

}
