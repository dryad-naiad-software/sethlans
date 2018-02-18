import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DualSetupComponent} from './dual-setup.component';

describe('DualSetupComponent', () => {
  let component: DualSetupComponent;
  let fixture: ComponentFixture<DualSetupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DualSetupComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DualSetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
