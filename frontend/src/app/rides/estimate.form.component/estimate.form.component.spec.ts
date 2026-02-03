import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EstimateFormComponent } from './estimate.form.component';

describe('EstimateFormComponent', () => {
  let component: EstimateFormComponent;
  let fixture: ComponentFixture<EstimateFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstimateFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EstimateFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
