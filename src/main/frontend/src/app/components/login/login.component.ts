import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Login} from "../../models/login.model";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  login: Login;
  loginError: boolean;

  constructor(private http: HttpClient, private auth: AuthService, private route: ActivatedRoute, private router: Router) {

  }

  ngOnInit() {
    this.login = new Login();
    this.route.queryParams.subscribe(params => {
      this.loginError = params['error'];
      console.log("Error present? " + this.loginError);
    });
    this.route.url.subscribe(url => {
      console.log(url);
    })

  }

  onSubmit() {
    let body = new URLSearchParams();
    body.set('username', this.login.username);
    body.set('password', this.login.password);


    this.http.post('login', body.toString(), {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
      responseType: 'text'
    }).subscribe(() => {
      this.auth.getAuthStatusAtLogin(this.login.username);
    });
  }

  registerUser() {
    this.router.navigateByUrl("/register").then(() => {
      location.reload(true);

    });
  }

  loginUser(event, form) {
    if (event.key === "Enter" && form.valid) {
      this.onSubmit();
    }

  }
}
