import {Component, OnInit} from '@angular/core';
import {UserInfo} from "../../../models/userinfo.model";
import {HttpClient} from "@angular/common/http";
import {Subject} from "rxjs/Subject";

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  userInfoList: UserInfo[];
  dtTrigger: Subject<any> = new Subject();

  constructor(private http: HttpClient) {

  }

  ngOnInit() {
    this.http.get('/api/management/user_list/').subscribe((userinfo: UserInfo[]) => {
      this.userInfoList = userinfo;
      console.log(this.userInfoList);
      this.dtTrigger.next();
    });
  }

}
