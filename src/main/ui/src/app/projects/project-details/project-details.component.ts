import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../../services/sethlans.service";
import {ActivatedRoute} from "@angular/router";
import {ProjectView} from "../../models/project/projectview.model";
import {BehaviorSubject} from "rxjs";
import {ProjectType} from 'src/app/enums/projectype.enum';
import {AnimationType} from 'src/app/enums/animationtype.enum';
import {ProjectState} from "../../enums/projectstate.enum";
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

@Component({
  selector: 'app-project-details',
  templateUrl: './project-details.component.html',
  styleUrls: ['./project-details.component.css']
})
export class ProjectDetailsComponent implements OnInit {
  project = new BehaviorSubject<ProjectView>(new ProjectView())
  projectImage = 'assets/images/placeholder.svg'
  ProjectType = ProjectType;
  AnimationType = AnimationType;
  projectID: string = ''
  ProjectState = ProjectState;
  timeStamp = new Date().getTime();
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



  constructor(private sethlansService: SethlansService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.projectID = params['id'];
      this.getProject();
    });
  }

  getThumbnail() {
    if(this.timeStamp) {
      return this.projectImage + '?' + this.timeStamp;
    }
    return this.projectImage;
  }

  setThumbnail() {
    this.projectImage = this.projectImage= '/api/v1/project/' + this.projectID + '/thumbnail/';
    this.timeStamp = new Date().getTime();


  }

  getProject() {
    this.sethlansService.getProject(this.projectID).subscribe((data: any) => {
      this.project.next(data);
      if(this.project.getValue().projectStatus.completedFrames >= 1) {
        this.setThumbnail();
      }
    });
    setTimeout(() => {
      this.getProject();
    }, 15000);

  }

  startProject() {
    this.sethlansService.startProject(this.projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProject();
      }
    })
  }

  stopProject() {
    this.sethlansService.stopProject(this.projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProject();
      }
    })
  }

  pauseProject() {
    this.sethlansService.pauseProject(this.projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProject();
      }
    })
  }

  resumeProject() {
    this.sethlansService.resumeProject(this.projectID).subscribe((response) => {
      if (response.statusText == 'Accepted') {
        this.getProject();
      }
    })
  }


  downloadProject() {
    window.location.href = '/api/v1/project/' + this.projectID + '/download_images/'
  }

  downloadVideo() {
    window.location.href = '/api/v1/project/' + this.projectID + '/download_video/'

  }

}
