import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NodeSetupComponent} from './node-setup.component';

describe('NodeSetupComponent', () => {
  let component: NodeSetupComponent;
  let fixture: ComponentFixture<NodeSetupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NodeSetupComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeSetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
