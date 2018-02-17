import {Component, Input, OnInit} from '@angular/core';
import {Title} from "@angular/platform-browser";

@Component({
  selector: 'app-setup-wizard',
  templateUrl: './setup-wizard.component.html',
  styleUrls: ['./setup-wizard.component.scss']
})
export class SetupWizardComponent implements OnInit {
  title = "Sethlans Setup Wizard";
  @Input() formData;

  constructor(private titleService: Title) {
  }

  ngOnInit() {
    this.titleService.setTitle(this.title);
  }

}
