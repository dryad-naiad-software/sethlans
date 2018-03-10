import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupRegisterUserComponent} from './setup-register-user.component';

describe('SetupRegisterUserComponent', () => {
  let component: SetupRegisterUserComponent;
  let fixture: ComponentFixture<SetupRegisterUserComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupRegisterUserComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupRegisterUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
