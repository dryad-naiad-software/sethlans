import {Component, OnInit} from '@angular/core';
import {Subject} from "rxjs/Subject";

@Component({
  selector: 'app-servers',
  templateUrl: './servers.component.html',
  styleUrls: ['./servers.component.scss']
})
export class ServersComponent implements OnInit {
  dtTrigger: Subject<any> = new Subject();

  constructor() {
  }

  ngOnInit() {
  }

}
