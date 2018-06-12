import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ComputeSettingsComponent} from './compute-settings.component';

describe('ComputeSettingsComponent', () => {
  let component: ComputeSettingsComponent;
  let fixture: ComponentFixture<ComputeSettingsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ComputeSettingsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ComputeSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
