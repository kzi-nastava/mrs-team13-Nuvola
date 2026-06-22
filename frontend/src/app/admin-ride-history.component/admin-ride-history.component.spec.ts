import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideHistoryAdminComponent } from './admin-ride-history.component';

describe('AdminRideHistoryComponent', () => {
  let component: RideHistoryAdminComponent;
  let fixture: ComponentFixture<RideHistoryAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideHistoryAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideHistoryAdminComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
