import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminDriverInfoComponent } from './admin.driver.info.component';

describe('AdminDriverInfoComponent', () => {
  let component: AdminDriverInfoComponent;
  let fixture: ComponentFixture<AdminDriverInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminDriverInfoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminDriverInfoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
