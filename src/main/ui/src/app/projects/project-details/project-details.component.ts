import {Component, OnInit} from '@angular/core';
import {SethlansService} from "../../services/sethlans.service";
import {ActivatedRoute} from "@angular/router";
import {ProjectView} from "../../models/project/projectview.model";
import {BehaviorSubject} from "rxjs";
import {ProjectType} from 'src/app/enums/projectype.enum';
import {AnimationType} from 'src/app/enums/animationtype.enum';
import {ProjectState} from "../../enums/projectstate.enum";

@Component({
  selector: 'app-project-details',
  templateUrl: './project-details.component.html',
  styleUrls: ['./project-details.component.css']
})
export class ProjectDetailsComponent implements OnInit {
  project = new BehaviorSubject<ProjectView>(new ProjectView())
  ProjectType = ProjectType;
  AnimationType = AnimationType;
  placeholder: any = 'assets/images/placeholder.svg';
  projectID: string = ''
  ProjectState = ProjectState;



  constructor(private sethlansService: SethlansService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.projectID = params['id'];
      this.getProject();
    });
  }

  getProject() {
    this.sethlansService.getProject(this.projectID).subscribe((data: any) => {
      this.project.next(data);
    });
    setTimeout(() => {
      this.getProject();
    }, 30000);

  }

}
