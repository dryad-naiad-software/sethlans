import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})

export class FooterComponent implements OnInit {
  sethlansVersion: string;
  sethlansMode: string;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/info/version', {responseType: 'text'})
      .subscribe((version: string) => {
        this.sethlansVersion = version;
      });
    this.http.get('/api/info/sethlans_mode', {responseType: 'text'})
      .subscribe((sethlansmode: string) => {
        this.sethlansMode = sethlansmode;
      });
  }

}
