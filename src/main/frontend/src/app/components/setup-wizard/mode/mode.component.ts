import {Component, Input, OnInit} from '@angular/core';
import {SetupFormDataService} from "../service/setupFormData.service";
import {Mode} from "../../../enums/mode";

@Component({
  selector: 'app-mode',
  templateUrl: './mode.component.html',
  styleUrls: ['./mode.component.scss']
})
export class ModeComponent implements OnInit {
  @Input() setupFormData;
  mode: any = Mode;

  constructor(private setupFormDataService: SetupFormDataService) {
  }

  save(mode: Mode) {
    this.setupFormDataService.setSethlansMode(mode);
  }


  ngOnInit() {
    this.setupFormData = this.setupFormDataService.getSetupFormData();
  }

}
