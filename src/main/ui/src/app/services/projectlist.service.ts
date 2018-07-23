import {Observable} from "rxjs/internal/Observable";
import {HttpClient} from "@angular/common/http";
import {Project} from "../models/project.model";
import {Injectable} from "@angular/core";

@Injectable()
export class ProjectListService {

  constructor(private http: HttpClient) {
  }

  getProjectList(): Observable<Project[]> {
    return this.http.get<Project[]>('/api/project_ui/project_list');
  }

  getProjectListInProgress(): Observable<Project[]> {
    return this.http.get<Project[]>('/api/project_ui/project_list_in_progress');
  }

  getProjectListSize(): Observable<number> {
    return this.http.get<number>("/api/project_ui/num_of_projects");
  }


}
