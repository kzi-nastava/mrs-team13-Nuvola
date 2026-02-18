import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideReportsComponent } from './ride-reports.component';

describe('RideReportsComponent', () => {
  let component: RideReportsComponent;
  let fixture: ComponentFixture<RideReportsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideReportsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideReportsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
