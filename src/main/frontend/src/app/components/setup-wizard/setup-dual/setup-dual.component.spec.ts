import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupDualComponent} from './setup-dual.component';

describe('SetupDualComponent', () => {
  let component: SetupDualComponent;
  let fixture: ComponentFixture<SetupDualComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupDualComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupDualComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
