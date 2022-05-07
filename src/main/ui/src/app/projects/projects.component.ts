import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../services/sethlans.service";
import {ProjectView} from "../models/project/projectview.model";
import {
  faCog,
  faEdit,
  faFilm,
  faForwardStep,
  faGear,
  faImages,
  faPause,
  faPlay,
  faPlus,
  faPuzzlePiece,
  faRectangleList,
  faSearchPlus,
  faStop,
  faTrashAlt,
  faUpload
} from "@fortawesome/free-solid-svg-icons";
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
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ProjectState} from "../enums/projectstate.enum";
import {BehaviorSubject} from "rxjs";


@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent implements OnInit {
  faStop = faStop;
  faTrashAlt = faTrashAlt;
  faEdit = faEdit;
  faPlus = faPlus;
  faUpload = faUpload;
  faPuzzlePiece = faPuzzlePiece;
  faFilm = faFilm;
  faGear = faGear;
  faCog = faCog;
  faPlay = faPlay;
  faPause = faPause;
  faImages = faImages;
  faSearchPlus = faSearchPlus;
  faForwardStep = faForwardStep;
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
  editProject = false;
  projectNameRegEx = new RegExp('.{4,}')
  detailsDisabled = true;
  settingsDisabled = true;
  summaryDisabled = true;
  submitDisabled = false;
  nextDisabled: boolean = false;
  projectNameError: boolean = false;
  placeholder: any = 'assets/images/placeholder.svg';
  selectedProject: ProjectView = new ProjectView();
  ProjectState = ProjectState;
  projectDataSource = new BehaviorSubject<any[]>([]);
  projectDisplayedColumns = new BehaviorSubject<string[]>([
    'projectName',
    'projectType',
    'computeOn',
    'fileFormat',
    'status',
    'progress',
    'preview',
    'action'
  ]);


  constructor(private modalService: NgbModal, private sethlansService: SethlansService) {

  }


  ngOnInit(): void {
    this.getProjects();
  }

  getProjects() {
    this.sethlansService.getProjects().subscribe((data: any) => {
      this.projectDataSource = new BehaviorSubject<any[]>(data);
    })
    setTimeout(() => {
      this.getProjects()
    }, 30000);
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

  startProject(projectID: string) {
    this.sethlansService.startProject(projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProjects();
      }
    })
  }

  stopProject(projectID: string) {
    this.sethlansService.stopProject(projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProjects();
      }
    })
  }

  pauseProject(projectID: string) {
    this.sethlansService.pauseProject(projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProjects();
      }
    })
  }

  resumeProject(projectID: string) {
    this.sethlansService.resumeProject(projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProjects();
      }
    })
  }


  downloadProject(projectID: string) {
    window.location.href = '/api/v1/project/' + projectID + '/download_images/'
  }

  downloadVideo(projectID: string) {
    window.location.href = '/api/v1/project/' + projectID + '/download_video/'

  }

  deleteProject(projectID: string) {
    this.sethlansService.deleteProject(projectID).subscribe((response) => {
      if (response.statusText == 'OK') {
        this.selectedProject = new ProjectView();
        this.getProjects();
      }
    })

  }

  deleteProjectModal(content: any, project: ProjectView) {
    this.selectedProject = project;
    this.modalService.open(content)
  }

  editProjectWizard(projectID: string) {
    this.editProject = true;
    this.projectWizardProgress = ProjectWizardProgress.PROJECT_DETAILS;
    this.projectWizardScreen = true;

  }

}
