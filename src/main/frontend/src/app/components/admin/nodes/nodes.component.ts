import {Component, OnInit} from '@angular/core';
import {Subject} from "rxjs/Subject";

@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.scss']
})
export class NodesComponent implements OnInit {
  dtTrigger: Subject<any> = new Subject();

  constructor() {
  }

  ngOnInit() {
  }

}
