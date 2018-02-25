import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {LocalStorageService, SessionStorageService} from "ngx-webstorage";


@Injectable()
export class AuthService {
  constructor(private http: HttpClient, private localStorage: LocalStorageService, private sessionStorage: SessionStorageService) {

  }

  getToken() {
    return this.localStorage.retrieve('authenticationToken') || this.sessionStorage.retrieve('authenticationToken')
  }

  login(login) {
    console.log("logging in");
    const data = {
      username: login.username,
      password: login.password,
      rememberMe: login.rememberMe
    };

    return this.http.post('/login', data, {observe: 'response'}).subscribe(authenticateSuccess.bind(this));

    function authenticateSuccess(resp) {
      const bearerToken = resp.headers.get('Authorization');
      if (bearerToken && bearerToken.slice(0, 7) === 'Bearer ') {
        const jwt = bearerToken.slice(7, bearerToken.length);
        this.storeAuthenticationToken(jwt, login.rememberMe);
        return jwt;
      }
    }
  }

  storeAuthenticationToken(jwt, rememberMe) {
    if (rememberMe) {
      this.localStorage.store('authenticationToken', jwt);
    } else {
      this.sessionStorage.store('authenticationToken', jwt);
    }

  }

  logout(): Observable<any> {
    return new Observable((observable) => {
      this.localStorage.clear('authenticationToken');
      this.sessionStorage.clear('authenticationToken');
    });
  }


}

