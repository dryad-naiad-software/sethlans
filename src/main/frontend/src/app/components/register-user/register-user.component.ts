import {Component, OnInit} from '@angular/core';
import {User} from "../../models/user.model";
import {Role} from "../../enums/role.enum";
import {Router} from "@angular/router";
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Component({
  selector: 'app-register-user',
  templateUrl: './register-user.component.html',
  styleUrls: ['./register-user.component.scss']
})
export class RegisterUserComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  user: User;
  userExists: boolean;

  constructor(private router: Router, private http: HttpClient) {
  }

  ngOnInit() {
    this.user = new User();

  }

  login() {
    this.router.navigateByUrl("/login");
  }

  submitUser(event, form) {
    if (event.key === "Enter" && form.valid) {
      this.onSubmit();
    }

  }

  onSubmit() {
    this.user.setActive(true);
    this.user.setPasswordUpdated(true);
    this.user.setRoles([Role.USER]);
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    this.http.post("/api/setup/register", JSON.stringify(this.user), httpOptions).subscribe((submitted: boolean) => {
      if (submitted === true) {
        this.login()
      } else {
        this.userExists = true;
      }
    });
  }

}
