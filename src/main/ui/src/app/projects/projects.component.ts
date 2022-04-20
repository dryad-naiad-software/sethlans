import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../services/sethlans.service";
import {ProjectView} from "../models/project/projectview.model";
import {faGear, faPlus, faPuzzlePiece, faRectangleList, faUpload} from "@fortawesome/free-solid-svg-icons";
import {ProjectWizardProgress} from "../enums/projectwizardprogress.enum";
import {ProjectForm} from "../models/forms/project-form.model";
import {ProjectType} from "../enums/projectype.enum";
import {ImageOutputFormat} from "../enums/imageoutputformat.enum";
import {BlenderEngine} from "../enums/blenderengine.enum";
import {ComputeOn} from "../enums/computeon.enum";


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
  faGear = faGear;
  faRectangleList = faRectangleList;
  projectWizardScreen: boolean = false;
  projectWizardProgress: ProjectWizardProgress = ProjectWizardProgress.UPLOAD;
  ProjectWizardProgress = ProjectWizardProgress;
  projectForm: ProjectForm = new ProjectForm();
  BlenderEngine = BlenderEngine;
  showInvalidFileAlert: boolean = false;
  ProjectType = ProjectType;
  ImageOutputFormat = ImageOutputFormat;
  showVideoSettings = false;
  ComputeOn = ComputeOn;


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

  loadProjectDetails($event: any) {
    this.projectForm.setProjectForm($event.originalEvent.body)
    console.log(this.projectForm)
    this.projectWizardProgress = ProjectWizardProgress.PROJECT_DETAILS;

  }

  resetUpload(fileUpload: any) {
    fileUpload.clear();
    this.showInvalidFileAlert = true;
  }

  setToPNG() {
    this.projectForm.projectSettings.imageSettings.imageOutputFormat = ImageOutputFormat.PNG;
  }

  cancelProjectWizard() {
    window.location.href = '/projects';
  }

  next() {
    switch (this.projectWizardProgress) {
      case ProjectWizardProgress.PROJECT_DETAILS:
        this.projectWizardProgress = ProjectWizardProgress.SETTINGS;
    }

  }

  previous() {
    switch (this.projectWizardProgress) {
      case ProjectWizardProgress.SETTINGS:
        this.projectWizardProgress = ProjectWizardProgress.PROJECT_DETAILS;
    }

  }

}
