import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ServerSetupComponent} from './server-setup.component';

describe('ServerSetupComponent', () => {
  let component: ServerSetupComponent;
  let fixture: ComponentFixture<ServerSetupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ServerSetupComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServerSetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
