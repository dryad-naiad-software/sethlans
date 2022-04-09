import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ServerDashComponent} from './server-dash.component';

describe('ServerDashComponent', () => {
  let component: ServerDashComponent;
  let fixture: ComponentFixture<ServerDashComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServerDashComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServerDashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
