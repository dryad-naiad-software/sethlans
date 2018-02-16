import {Component, Injectable, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

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

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/info/first_time').subscribe((firstTime: boolean) => this.firstTime = firstTime);
  }
}
