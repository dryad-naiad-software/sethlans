import {Component, OnInit} from '@angular/core';
import {ServerDashboard} from "../../models/views/server-dashboard.model";
import {SethlansService} from "../../services/sethlans.service";
import {ProjectView} from "../../models/project/projectview.model";
import {ProjectState} from "../../enums/projectstate.enum";
import {BehaviorSubject} from "rxjs";
import {faSquare} from "@fortawesome/free-solid-svg-icons";
import {Mode} from 'src/app/enums/mode.enum';


@Component({
  selector: 'app-server-dash',
  templateUrl: './server-dash.component.html',
  styleUrls: ['./server-dash.component.css']
})
export class ServerDashComponent implements OnInit {
  mode: Mode = Mode.SETUP;
  Mode = Mode;
  faSquare = faSquare;
  serverDashboard = new ServerDashboard();
  activeProjects: ProjectView[] = [];
  projectDataSource = new BehaviorSubject<any[]>([]);
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
    this.getProjects();

  }

  getProjects() {
    this.sethlansService.mode().subscribe((data: any) => {
      this.mode = data.mode;
    });
    this.sethlansService.getProjects().subscribe((data: any) => {
      let projects: ProjectView[] = data;
      this.activeProjects = [];
      for (const projectsKey in projects) {
        if (projects[projectsKey].projectStatus.projectState != ProjectState.FINISHED
          && projects[projectsKey].projectStatus.projectState != ProjectState.STOPPED && projects[projectsKey].projectStatus.projectState != ProjectState.ADDED) {
          this.activeProjects.push(projects[projectsKey]);
        }
      }
      this.projectDataSource = new BehaviorSubject<any[]>(this.activeProjects);
    })
    setTimeout(() => {
      this.getProjects()
    }, 15000);
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
    }, 30000);
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
