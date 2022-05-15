import {Component, OnInit} from '@angular/core';
import {ServerDashboard} from "../../models/views/server-dashboard.model";
import {SethlansService} from "../../services/sethlans.service";

@Component({
  selector: 'app-server-dash',
  templateUrl: './server-dash.component.html',
  styleUrls: ['./server-dash.component.css']
})
export class ServerDashComponent implements OnInit {
  serverDashboard = new ServerDashboard();
  activeNodes = 0;
  chartData: { labels: string[]; datasets: { data: any; backgroundColor: string[]; hoverBackgroundColor: string[] }[] };

  constructor(private sethlansService: SethlansService) {
    this.chartData = {
      labels: ['CPU', 'GPU', 'CPU_GPU'],
      datasets: [
        {
          data: [0, 0, 0],
          backgroundColor: [
            "#43C519",
            "#1943C5",
            "#C51943"
          ],
          hoverBackgroundColor: [
            "#43C519",
            "#1943C5",
            "#C51943"
          ]
        }]
    };
  }

  ngOnInit(): void {
    this.getDashboard();

  }

  getDashboard() {
    this.sethlansService.getServerDashBoard().subscribe((obj: any) => {
      this.serverDashboard = obj;
      if (this.activeNodes != obj.activeNodes) {
        this.updateChart()
        this.activeNodes = obj.activeNodes;
      }

    })
    setTimeout(() => {
      this.getDashboard();
    }, 15000);
  }

  updateChart() {
    this.chartData = {
      labels: ['CPU_GPU', 'GPU', 'CPU'],
      datasets: [
        {
          data: this.serverDashboard.nodeDistribution,
          backgroundColor: [
            "#43C519",
            "#1943C5",
            "#C51943"
          ],
          hoverBackgroundColor: [
            "#43C519",
            "#1943C5",
            "#C51943"
          ]
        }]
    };
  }

}
