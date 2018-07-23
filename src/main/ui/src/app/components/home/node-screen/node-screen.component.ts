import {Component, OnInit} from '@angular/core';
import {NodeDashboard} from "../../../models/nodeDash.model";
import {HttpClient} from "@angular/common/http";
import {timer} from "rxjs/internal/observable/timer";
import {ComputeMethod} from "../../../enums/compute.method.enum";

@Component({
  selector: 'app-node-screen',
  templateUrl: './node-screen.component.html',
  styleUrls: ['./node-screen.component.scss']
})
export class NodeScreenComponent implements OnInit {
  nodeDash: NodeDashboard;
  method: any = ComputeMethod;


  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.getInfo();
    let scheduler = timer(5000, 5000);
    scheduler.subscribe(() => this.getInfo());
  }

  getInfo() {
    this.http.get('/api/info/node_dashboard').subscribe((nodeDash: NodeDashboard) => {
      this.nodeDash = nodeDash;
    });
  }

}
