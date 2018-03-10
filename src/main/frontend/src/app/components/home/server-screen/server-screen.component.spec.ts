import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ServerScreenComponent} from './server-screen.component';

describe('ServerScreenComponent', () => {
  let component: ServerScreenComponent;
  let fixture: ComponentFixture<ServerScreenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ServerScreenComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ServerScreenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
