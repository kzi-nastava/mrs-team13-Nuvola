import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRidesComponent } from './driver.rides.component';

describe('DriverRidesComponent', () => {
  let component: DriverRidesComponent;
  let fixture: ComponentFixture<DriverRidesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRidesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRidesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
