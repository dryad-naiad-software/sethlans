import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupFinishedComponent} from './setup-finished.component';

describe('SetupFinishedComponent', () => {
  let component: SetupFinishedComponent;
  let fixture: ComponentFixture<SetupFinishedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupFinishedComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupFinishedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
