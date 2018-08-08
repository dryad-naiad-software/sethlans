import {Component, OnInit} from '@angular/core';
import {ProjectWizardForm} from '../../../models/forms/project_wizard_form.model';
import {ProjectWizardProgress} from '../../../enums/project_wizard_progress';
import {HttpClient, HttpHeaders} from '../../../../../node_modules/@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {Project} from '../../../models/project.model';

@Component({
  selector: 'app-project-edit-wizard',
  templateUrl: './project-edit-wizard.component.html',
  styleUrls: ['./project-edit-wizard.component.scss']
})
export class ProjectEditWizardComponent implements OnInit {
  projectWizard: ProjectWizardForm;
  wizardProgress: any = ProjectWizardProgress;
  nextDisabled: boolean;
  id: number;


  constructor(private http: HttpClient, private route: ActivatedRoute) {
    this.route.params.subscribe(params => {
      this.id = +params['id'];
    });
    document.body.style.background = 'rgba(0, 0, 0, .6)';
    this.projectWizard = new ProjectWizardForm();
    this.projectWizard.currentProgress = ProjectWizardProgress.PROJECT_DETAILS;
  }

  ngOnInit() {
    this.getAvailableBlenderVersions();
    this.http.get('/api/project_ui/project_details/' + this.id + '/').subscribe((projectDetails: Project) => {
      this.projectWizard.project = projectDetails;
      this.projectWizard.projectLoaded = true;
    });
  }

  getAvailableBlenderVersions() {
    this.http.get('/api/info/installed_blender_versions')
      .subscribe(
        (blenderVersions: string[]) => {
          this.projectWizard.availableBlenderVersions = blenderVersions;
        });
  }

  returnToProjects(): void {
    window.location.href = '/projects/';
  }

  disableNext(value: boolean) {
    this.nextDisabled = value;
  }

  editProject() {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      })
    };
    if (this.projectWizard.project.useParts == false) {
      this.projectWizard.project.partsPerFrame = 1;
    }
    this.http.post('/api/project_form/edit_project/' + this.id + '/', JSON.stringify(this.projectWizard.project), httpOptions).subscribe((success: boolean) => {
      this.returnToProjects();
    });
  }

  next() {
    switch (this.projectWizard.currentProgress) {
      case ProjectWizardProgress.PROJECT_DETAILS:
        this.projectWizard.detailsValid = true;
        this.projectWizard.currentProgress = ProjectWizardProgress.RENDER_SETTINGS;
        break;
      case ProjectWizardProgress.RENDER_SETTINGS:
        this.projectWizard.formComplete = true;
        this.projectWizard.currentProgress = ProjectWizardProgress.SUMMARY;
        break;
    }
  }

  previous() {
    switch (this.projectWizard.currentProgress) {
      case ProjectWizardProgress.RENDER_SETTINGS:
        this.projectWizard.currentProgress = ProjectWizardProgress.PROJECT_DETAILS;
        break;
      case ProjectWizardProgress.SUMMARY:
        this.projectWizard.currentProgress = ProjectWizardProgress.RENDER_SETTINGS;
        break;
    }
  }

}
