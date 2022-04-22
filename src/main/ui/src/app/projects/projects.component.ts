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
import {VideoSettings} from "../models/settings/videosettings.model";
import {VideoOutputFormat} from "../enums/videooutputformat.enum";
import {VideoCodec} from "../enums/videocodec.enum";
import {PixelFormat} from "../enums/pixelformat.enum";
import {VideoQuality} from "../enums/videoquality.enum";
import {AnimationType} from "../enums/animationtype.enum";


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
  VideoOutputFormat = VideoOutputFormat;
  VideoCodec = VideoCodec;
  PixelFormat = PixelFormat;
  VideoQuality = VideoQuality;
  AnimationType = AnimationType;
  projectNameRegEx = new RegExp('.{4,}')
  detailsDisabled = true;
  settingsDisabled = true;
  summaryDisabled = true;
  submitDisabled = false;
  nextDisabled: boolean = false;
  projectNameError: boolean = false;
  placeholder: any = 'assets/images/placeholder.svg';


  constructor(private sethlansService: SethlansService) {

  }


  ngOnInit(): void {
    this.sethlansService.getProjects().subscribe((data: any) => {
      this.projectList = data;
      console.log(this.projectList)
    })
  }

  startProjectWizard() {
    this.projectWizardScreen = true;
  }

  loadProjectDetails($event: any) {
    this.projectForm.setProjectForm($event.originalEvent.body)
    this.projectForm.projectSettings.videoSettings = new VideoSettings();
    this.projectForm.projectSettings.animationType = AnimationType.MOVIE;
    this.projectWizardProgress = ProjectWizardProgress.PROJECT_DETAILS;
    this.detailsDisabled = false;
    this.nextDisabled = true;

  }

  validateProjectName() {
    this.projectNameError = !this.projectForm.projectName.match(this.projectNameRegEx);
    this.nextDisabled = this.projectNameError;

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
        this.settingsDisabled = false;
        break;
      case ProjectWizardProgress.SETTINGS:
        this.projectWizardProgress = ProjectWizardProgress.SUMMARY;
    }

  }

  previous() {
    switch (this.projectWizardProgress) {
      case ProjectWizardProgress.SETTINGS:
        this.projectWizardProgress = ProjectWizardProgress.PROJECT_DETAILS;
        break;
      case ProjectWizardProgress.SUMMARY:
        this.projectWizardProgress = ProjectWizardProgress.SETTINGS;
    }

  }

  resetCodec() {
    if (this.projectForm.projectSettings.videoSettings.videoOutputFormat == VideoOutputFormat.AVI) {
      this.projectForm.projectSettings.videoSettings.codec = VideoCodec.FFV1;
    }
    if (this.projectForm.projectSettings.videoSettings.videoOutputFormat == VideoOutputFormat.MP4 ||
      this.projectForm.projectSettings.videoSettings.videoOutputFormat == VideoOutputFormat.MKV) {
      this.projectForm.projectSettings.videoSettings.codec = VideoCodec.LIBX264;

    }
  }

  submit() {
    if (this.projectForm.projectType == ProjectType.STILL_IMAGE) {
      this.projectForm.projectSettings.stepFrame = 1
      this.projectForm.projectSettings.endFrame = this.projectForm.projectSettings.startFrame;
      this.projectForm.projectSettings.animationType = AnimationType.IMAGES;
    }
    this.submitDisabled = true;
    this.sethlansService.submitProject(this.projectForm).subscribe((response) => {
      if (response.statusText == "Created") {
        window.location.href = '/projects';
      }

    });

  }

  resetQuality() {

    if (this.projectForm.projectSettings.videoSettings.codec == VideoCodec.LIBX264) {
      this.projectForm.projectSettings.videoSettings.videoQuality = VideoQuality.LOW_X264;
    }
    if (this.projectForm.projectSettings.videoSettings.codec == VideoCodec.LIBX265) {
      this.projectForm.projectSettings.videoSettings.videoQuality = VideoQuality.LOW_X265;
    }


  }

}
