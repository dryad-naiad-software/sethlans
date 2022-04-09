import {Component, OnInit} from '@angular/core';
import {faKey} from "@fortawesome/free-solid-svg-icons";


@Component({
  selector: 'app-authorized-server',
  templateUrl: './authorized-server.component.html',
  styleUrls: ['./authorized-server.component.css']
})
export class AuthorizedServerComponent implements OnInit {
  faKey = faKey;

  constructor() {
  }

  ngOnInit(): void {
  }

}
