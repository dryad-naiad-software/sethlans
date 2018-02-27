import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  logo: any = "assets/images/logo.png";
  username: string;
  authenticated: boolean;
  isCollapsed = true;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/users/username', {responseType: 'text'})
      .subscribe((user: string) => {
        if (user.indexOf('<') >= 0) {
          this.authenticated = false;
        }
        else {
          this.authenticated = true;
          this.username = user;
        }

      });
  }

}
