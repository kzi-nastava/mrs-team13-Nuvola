import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverAccountComponent } from './driver.account.component';

describe('DriverAccountComponent', () => {
  let component: DriverAccountComponent;
  let fixture: ComponentFixture<DriverAccountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverAccountComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverAccountComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
