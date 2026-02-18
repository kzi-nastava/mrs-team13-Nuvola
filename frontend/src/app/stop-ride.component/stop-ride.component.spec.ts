import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StopRideComponent } from './stop-ride.component';

describe('StopRideComponent', () => {
  let component: StopRideComponent;
  let fixture: ComponentFixture<StopRideComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StopRideComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StopRideComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
