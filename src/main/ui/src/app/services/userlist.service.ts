import {Injectable} from "@angular/core";
import {Observable} from "rxjs/internal/Observable";
import {HttpClient} from "@angular/common/http";
import {UserInfo} from "../models/userinfo.model";

@Injectable()
export class UserListService {

  constructor(private http: HttpClient) {
  }

  getUserList(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>('/api/management/user_list/');
  }
}
