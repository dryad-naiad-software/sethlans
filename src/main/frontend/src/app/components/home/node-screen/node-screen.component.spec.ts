import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NodeScreenComponent} from './node-screen.component';

describe('NodeScreenComponent', () => {
  let component: NodeScreenComponent;
  let fixture: ComponentFixture<NodeScreenComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NodeScreenComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeScreenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
