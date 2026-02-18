import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideDetailsAdminComponent } from './ride-details-admin.component';

describe('RideDetailsAdminComponent', () => {
  let component: RideDetailsAdminComponent;
  let fixture: ComponentFixture<RideDetailsAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideDetailsAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideDetailsAdminComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
