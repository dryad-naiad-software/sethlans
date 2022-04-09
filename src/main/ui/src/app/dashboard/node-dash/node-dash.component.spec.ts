import {ComponentFixture, TestBed} from '@angular/core/testing';

import {NodeDashComponent} from './node-dash.component';

describe('NodeDashComponent', () => {
  let component: NodeDashComponent;
  let fixture: ComponentFixture<NodeDashComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NodeDashComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeDashComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
