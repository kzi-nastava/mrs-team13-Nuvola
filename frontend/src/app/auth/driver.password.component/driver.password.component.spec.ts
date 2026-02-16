import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverPasswordComponent } from './driver.password.component';

describe('DriverPasswordComponent', () => {
  let component: DriverPasswordComponent;
  let fixture: ComponentFixture<DriverPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverPasswordComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverPasswordComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
