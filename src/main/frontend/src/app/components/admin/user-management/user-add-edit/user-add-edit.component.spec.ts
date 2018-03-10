import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UserAddEditComponent} from './user-add-edit.component';

describe('UserAddEditComponent', () => {
  let component: UserAddEditComponent;
  let fixture: ComponentFixture<UserAddEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserAddEditComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAddEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
