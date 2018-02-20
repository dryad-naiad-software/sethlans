import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupSettingsComponent} from './setup-settings.component';

describe('SetupSettingsComponent', () => {
  let component: SetupSettingsComponent;
  let fixture: ComponentFixture<SetupSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupSettingsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
