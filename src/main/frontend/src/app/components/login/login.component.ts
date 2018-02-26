import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Login} from "../../models/login.model";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  login: Login;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.login = new Login();
  }

  onSubmit() {
    let body = new URLSearchParams();
    body.set('username', this.login.username);
    body.set('password', this.login.password);

    let options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    this.http.post('/login', body.toString(), options).subscribe();
  }

}
