import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AuthorizedServerComponent} from './authorized-server.component';

describe('AuthorizedServerComponent', () => {
  let component: AuthorizedServerComponent;
  let fixture: ComponentFixture<AuthorizedServerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AuthorizedServerComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthorizedServerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
