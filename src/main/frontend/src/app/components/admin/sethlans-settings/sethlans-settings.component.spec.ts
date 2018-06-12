import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SethlansSettingsComponent} from './sethlans-settings.component';

describe('SethlansSettingsComponent', () => {
  let component: SethlansSettingsComponent;
  let fixture: ComponentFixture<SethlansSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SethlansSettingsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SethlansSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
