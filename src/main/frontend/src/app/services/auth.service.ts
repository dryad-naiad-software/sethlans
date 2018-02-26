import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";

@Injectable()
export class AuthService {
  authenticated = false;

  constructor(private http: HttpClient) {

  }

  getAuthStatus(username) {
    this.http.get('/api/users/username', {responseType: 'text'}).subscribe((user: string) => {
      console.log(user);
      if (user.toLowerCase() === username.toLowerCase()) {
        this.authenticated = true;
      } else {
        this.authenticated = false;
      }
      return this.authenticated;
    })
  }
}
