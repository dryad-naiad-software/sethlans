import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-restart',
  templateUrl: './restart.component.html',
  styleUrls: ['./restart.component.scss']
})
export class RestartComponent implements OnInit {

  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
  }

  ngOnInit() {
    this.http.get("/api/management/restart").subscribe(() => {
      setTimeout(() => {
        window.location.href = "/";
      }, 30000);
    });

  }
}

