import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupServerComponent} from './setup-server.component';

describe('SetupServerComponent', () => {
  let component: SetupServerComponent;
  let fixture: ComponentFixture<SetupServerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupServerComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupServerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
