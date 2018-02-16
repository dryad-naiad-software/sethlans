import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {version} from "punycode";

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})

export class FooterComponent implements OnInit {
  version: string;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/info/version', {responseType: 'text'}).subscribe((version: string) => this.version = version);
    console.log(version);
  }

}
