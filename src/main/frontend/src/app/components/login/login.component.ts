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
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post("/login", JSON.stringify(this.login), httpOptions).subscribe();

  }

}
