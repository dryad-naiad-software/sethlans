import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ProjectEditWizardComponent} from './project-edit-wizard.component';

describe('ProjectEditWizardComponent', () => {
  let component: ProjectEditWizardComponent;
  let fixture: ComponentFixture<ProjectEditWizardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ProjectEditWizardComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectEditWizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
