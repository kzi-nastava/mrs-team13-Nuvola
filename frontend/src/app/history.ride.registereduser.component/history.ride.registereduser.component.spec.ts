import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoryRideRegistereduserComponent } from './history.ride.registereduser.component';

describe('HistoryRideRegistereduserComponent', () => {
  let component: HistoryRideRegistereduserComponent;
  let fixture: ComponentFixture<HistoryRideRegistereduserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoryRideRegistereduserComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoryRideRegistereduserComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
