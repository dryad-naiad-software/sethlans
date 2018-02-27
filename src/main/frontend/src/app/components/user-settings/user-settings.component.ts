import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {UserInfo} from "../../models/userinfo.model";


@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {
  username: string;
  userInfo: UserInfo;
  changePass = false;

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/users/username', {responseType: 'text'})
      .subscribe((user: string) => {
        this.username = user;
        this.http.get('/api/users/get_user/' + this.username + '').subscribe((userinfo: UserInfo) => {
          this.userInfo = userinfo;
          console.log(this.userInfo);
        });
      });

  }

  submitForm(event, form) {

  }

}
