import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideDetailsComponent } from './ride-details-admin.component';

describe('RideDetailsAdminComponent', () => {
  let component: RideDetailsComponent;
  let fixture: ComponentFixture<RideDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideDetailsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
