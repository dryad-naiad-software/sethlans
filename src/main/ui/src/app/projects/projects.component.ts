import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../services/sethlans.service";
import {ProjectView} from "../models/project/projectview.model";
import {faCube, faPlus, faPuzzlePiece, faRectangleList, faUpload} from "@fortawesome/free-solid-svg-icons";
import {ProjectWizardProgress} from "../enums/projectwizardprogress.enum";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent implements OnInit {
  projectList: Array<ProjectView> = []
  faPlus = faPlus;
  faUpload = faUpload;
  faPuzzlePiece = faPuzzlePiece;
  faCube = faCube;
  faRectangleList = faRectangleList;
  projectWizardScreen: boolean = false;
  projectWizardProgress: ProjectWizardProgress = ProjectWizardProgress.UPLOAD;
  ProjectWizardProgress = ProjectWizardProgress;

  constructor(private sethlansService: SethlansService) {

  }


  ngOnInit(): void {
    this.sethlansService.getProjects().subscribe((data: any) => {
      this.projectList = data;
    })
  }

  startProjectWizard() {
    this.projectWizardScreen = true;
  }

}
