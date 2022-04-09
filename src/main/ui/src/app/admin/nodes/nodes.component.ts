import {Component, OnInit} from '@angular/core';
import {faDownload, faPlus} from "@fortawesome/free-solid-svg-icons";


@Component({
  selector: 'app-nodes',
  templateUrl: './nodes.component.html',
  styleUrls: ['./nodes.component.css']
})
export class NodesComponent implements OnInit {
  faPlus = faPlus;
  faDownload = faDownload;

  constructor() {
  }

  ngOnInit(): void {
  }

}
