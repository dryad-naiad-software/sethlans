import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";

@Injectable()
export class AuthService {
  authenticated;

  constructor(private http: HttpClient, private router: Router) {

  }

  getAuthStatusAtLogin(username) {
    this.http.get('/api/users/username', {responseType: 'text'}).subscribe((user: string) => {
      console.log(user);
      this.authenticated = user.toLowerCase() === username.toLowerCase();
      if (this.authenticated == true) {
        this.router.navigateByUrl("/").then(() => {
          location.reload();

        });
      } else {
        this.authenticated = false;
        this.router.navigateByUrl("/login?error=true");
      }

    });
  }

  getAuthenticated(): boolean {
    return this.authenticated;
  }
}
