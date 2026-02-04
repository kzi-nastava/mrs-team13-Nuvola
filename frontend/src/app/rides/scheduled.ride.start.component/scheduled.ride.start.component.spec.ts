import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduledRideStartComponent } from './scheduled.ride.start.component';

describe('ScheduledRideStartComponent', () => {
  let component: ScheduledRideStartComponent;
  let fixture: ComponentFixture<ScheduledRideStartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduledRideStartComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScheduledRideStartComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
