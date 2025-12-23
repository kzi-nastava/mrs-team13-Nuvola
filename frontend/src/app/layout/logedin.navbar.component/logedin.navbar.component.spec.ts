import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogedinNavbarComponent } from './logedin.navbar.component';

describe('LogedinNavbarComponent', () => {
  let component: LogedinNavbarComponent;
  let fixture: ComponentFixture<LogedinNavbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogedinNavbarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogedinNavbarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
