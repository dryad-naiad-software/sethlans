import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupNetworkingComponent} from './setup-networking.component';

describe('SetupNetworkingComponent', () => {
  let component: SetupNetworkingComponent;
  let fixture: ComponentFixture<SetupNetworkingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupNetworkingComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupNetworkingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
