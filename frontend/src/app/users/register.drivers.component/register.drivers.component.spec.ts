import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterDriversComponent } from './register.drivers.component';

describe('RegisterDriversComponent', () => {
  let component: RegisterDriversComponent;
  let fixture: ComponentFixture<RegisterDriversComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterDriversComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterDriversComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
