import {Component, OnInit} from '@angular/core';
import {Subject} from "rxjs/Subject";

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  dtTrigger: Subject<any> = new Subject();

  constructor() {
  }

  ngOnInit() {
  }

}
