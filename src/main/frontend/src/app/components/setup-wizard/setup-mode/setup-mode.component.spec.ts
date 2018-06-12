import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupModeComponent} from './setup-mode.component';

describe('SetupModeComponent', () => {
  let component: SetupModeComponent;
  let fixture: ComponentFixture<SetupModeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupModeComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupModeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
