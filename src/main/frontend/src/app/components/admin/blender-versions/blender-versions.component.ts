import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {BlenderBinary} from "../../../models/blenderbinary.model";

@Component({
  selector: 'app-blender-versions',
  templateUrl: './blender-versions.component.html',
  styleUrls: ['./blender-versions.component.scss']
})
export class BlenderVersionsComponent implements OnInit {
  blenderVersions: string[];
  blenderBinaries: BlenderBinary[] = [];
  availableBlenderVersions: any[];

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get('/api/management/blender_versions/').subscribe((blenderVersions: string[]) => {
      this.blenderVersions = blenderVersions;
    });
    this.http.get('/api/info/blender_versions')
      .subscribe(
        (blenderVersions: any[]) => {
          this.availableBlenderVersions = blenderVersions;
          console.log(this.availableBlenderVersions);
        }, (error) => console.log(error));
  }

}
