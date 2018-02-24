import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  username: string;
  password: string;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
  }

  onSubmit() {

  }

}
