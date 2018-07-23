import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ServerDashboard} from "../../../models/serverDash.model";
import {HttpClient} from "@angular/common/http";
import {ProjectListService} from "../../../services/projectlist.service";
import {MatPaginator, MatSort, MatTableDataSource} from "@angular/material";
import {ProjectStatus} from "../../../enums/project_status.enum";
import {Mode} from "../../../enums/mode.enum";
import {timer} from "rxjs/internal/observable/timer";
import Utils from "../../../utils/utils";

@Component({
  selector: 'app-server-screen',
  templateUrl: './server-screen.component.html',
  styleUrls: ['./server-screen.component.scss']
})
export class ServerScreenComponent implements OnInit {
  serverDash: ServerDashboard;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  dataSource = new MatTableDataSource();
  displayedColumns = ['projectName', 'projectStatus', 'progress'];
  projectStatus: any = ProjectStatus;
  mode: any = Mode;
  projectSize: number;
  activeNodes: number;
  @Input() currentMode: Mode;
  chartData: { labels: string[]; datasets: { data: any; backgroundColor: string[]; hoverBackgroundColor: string[] }[] };
  currentPercentageArray: number[];
  currentStatusArray: ProjectStatus[];


  constructor(private http: HttpClient, private projectService: ProjectListService) {
    this.currentPercentageArray = [];
    this.currentStatusArray = [];
    this.serverDash = new ServerDashboard();
    this.activeNodes = 0;
  }

  ngOnInit() {
    this.getInfo();
    this.projectLoad();
    let scheduler = timer(5000, 5000);
    scheduler.subscribe(() => this.getInfo())

  }

  getInfo() {
    this.projectService.getProjectListSize().subscribe(value => {
      if (this.projectSize != value) {
        this.projectLoad();
      }
      this.projectSize = value
    });
    this.http.get('/api/info/server_dashboard').subscribe((serverDashboard: ServerDashboard) => {
      this.serverDash = serverDashboard;
      if (this.activeNodes != this.serverDash.activeNodes) {
        this.chartLoad();
      }
      this.activeNodes = this.serverDash.activeNodes;
    });
    this.projectService.getProjectListInProgress().subscribe(value => {
      let newPercentageArray: number[] = [];
      let newStatusArray: ProjectStatus[] = [];
      for (let i = 0; i < value.length; i++) {
        newPercentageArray.push(value[i].currentPercentage);
        newStatusArray.push(value[i].projectStatus);
      }
      if (!Utils.isEqual(newPercentageArray, this.currentPercentageArray)) {
        this.projectLoad();
      }

      if (!Utils.isEqual(newStatusArray, this.currentStatusArray)) {
        this.projectLoad();
      }

      this.currentPercentageArray = newPercentageArray;
      this.currentStatusArray = newStatusArray;
    });

  }

  projectLoad() {
    this.projectService.getProjectList().subscribe(data => {
      this.dataSource = new MatTableDataSource<any>(data);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    });
  }

  chartLoad() {
    this.chartData = {
      labels: ['CPU', 'GPU', 'CPU_GPU'],
      datasets: [
        {
          data: this.serverDash.numberOfActiveNodesArray,
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