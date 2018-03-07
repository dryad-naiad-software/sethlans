import {Component, OnInit} from '@angular/core';
import {Subject} from "rxjs/Subject";

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  placeholder: any = "assets/images/placeholder.svg";
  dtTrigger: Subject<any> = new Subject();

  constructor() {
  }

  ngOnInit() {
  }

}
