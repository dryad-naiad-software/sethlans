import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

@Injectable()
export class MetricsService {
  constructor(private http: HttpClient) {
  }

  getMetrics(): Observable<any> {
    return this.http.get('/api/management/metrics');
  }

  threadDump(): Observable<any> {
    return this.http.get('/api/management/dump/');
  }
}
