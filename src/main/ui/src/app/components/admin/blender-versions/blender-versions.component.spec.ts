import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BlenderVersionsComponent} from './blender-versions.component';

describe('BlenderVersionsComponent', () => {
  let component: BlenderVersionsComponent;
  let fixture: ComponentFixture<BlenderVersionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BlenderVersionsComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BlenderVersionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
