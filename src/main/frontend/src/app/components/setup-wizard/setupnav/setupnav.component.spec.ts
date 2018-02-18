import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {SetupnavComponent} from './setupnav.component';

describe('SetupnavComponent', () => {
  let component: SetupnavComponent;
  let fixture: ComponentFixture<SetupnavComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SetupnavComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SetupnavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
