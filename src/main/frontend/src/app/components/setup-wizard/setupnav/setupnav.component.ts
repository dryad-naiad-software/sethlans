import {Component, Input, OnInit} from '@angular/core';
import {SetupProgress} from "../../../enums/setupProgress";
import {SetupFormDataService} from "../service/setupFormData.service";

@Component({
  selector: 'app-setupnav',
  templateUrl: './setupnav.component.html',
  styleUrls: ['./setupnav.component.scss']
})
export class SetupnavComponent implements OnInit {
  progress: any = SetupProgress;
  @Input() setupFormData;


  constructor(private setupFormDataService: SetupFormDataService) {
  }

  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

}
