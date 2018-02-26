import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Login} from "../../models/login.model";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  login: Login;

  constructor(private http: HttpClient, private auth: AuthService) {
  }

  ngOnInit() {
    this.login = new Login();
  }

  onSubmit() {
    let body = new URLSearchParams();
    body.set('username', this.login.username);
    body.set('password', this.login.password);


    this.http.post('login', body.toString(), {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text'
    }).subscribe(() => {
      let status = this.auth.getAuthStatus(this.login.username);
      console.log(status)
    });
  }

}
