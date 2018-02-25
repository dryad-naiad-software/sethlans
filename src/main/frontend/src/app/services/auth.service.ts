import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService, SessionStorageService} from "ngx-webstorage";
import {Observable} from "rxjs/Observable";
import "rxjs/add/operator/map";


@Injectable()
export class AuthService {


  constructor(private http: HttpClient, private localStorage: LocalStorageService, private sessionStorage: SessionStorageService) {

  }

  getToken() {
    return this.localStorage.retrieve('authenticationToken') || this.sessionStorage.retrieve('authenticationToken')
  }

  login(login): Observable<any> {
    const data = {
      username: login.username,
      password: login.password,
      rememberMe: login.rememberMe
    };
    return this.http
      .post('/login', data, {observe: 'response'}).map(authenticateSuccess.bind(this));

    function authenticateSuccess(resp) {
      const bearerToken = resp.headers.get('Authorization');
      if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
        const jwt = bearerToken.slice(7, bearerToken.length);
        this.storeAuthenticationToken(jwt, login.rememberMe);
        return jwt;
      }
    }
  }


}

