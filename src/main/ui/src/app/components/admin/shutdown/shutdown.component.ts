import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-shutdown',
  templateUrl: './shutdown.component.html',
  styleUrls: ['./shutdown.component.scss']
})
export class ShutdownComponent implements OnInit {

  constructor(private http: HttpClient) {
    document.body.style.background = 'rgba(0, 0, 0, .6)';
  }

  ngOnInit() {
    this.http.get("/api/management/shutdown").subscribe();
  }

}
