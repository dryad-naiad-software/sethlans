import {Component, OnInit} from '@angular/core';
import {Login} from "../models/system/login.model";
import {Router} from "@angular/router";
import {LoginService} from "../services/login.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  logo: any = 'assets/images/logo-text-white.png';
  login: Login;
  loginError: any;


  constructor(private auth: LoginService, private router: Router) {
    this.login = new Login();
  }

  ngOnInit(): void {


  }

  loginSubmit() {
    this.auth.authenticate(this.login, () => {
      this.router.navigateByUrl('/login');
    });
  }

  registerUser() {
    window.location.href = '/register';
  }

  loginUser(event: { key: string; }, form: { valid: any; }) {
    if (event.key === 'Enter' && form.valid) {
      this.loginSubmit();
    }
  }

}
