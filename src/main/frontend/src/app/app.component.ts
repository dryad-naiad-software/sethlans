import {Component, Injectable, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {AuthService} from "./services/auth.service";
import 'rxjs/add/operator/finally';
import {Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

@Injectable()
export class AppComponent implements OnInit {
  title = 'Sethlans';
  firstTime: boolean;
  logo: any = "assets/images/logo.png";

  constructor(private http: HttpClient, private auth: AuthService, private router: Router) {
    this.http.get('/api/info/first_time').subscribe((firstTime: boolean) => this.firstTime = firstTime);
  }

  logout() {
    this.http.post('logout', {}).finally(() => {
      this.auth.authenticated = false;
      this.router.navigateByUrl('/login');
    }).subscribe();
  }

  ngOnInit() {

  }
}
