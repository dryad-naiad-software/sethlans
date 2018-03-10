import {Component, OnInit} from '@angular/core';
import {Mode} from "../../enums/mode.enum";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  currentMode: Mode;
  mode: any = Mode;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/info/sethlans_mode', {responseType: 'text'})
      .subscribe((sethlansmode: Mode) => {
        this.currentMode = sethlansmode;
      });
  }

}
