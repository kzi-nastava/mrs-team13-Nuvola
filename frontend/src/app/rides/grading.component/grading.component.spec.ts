import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GradingComponent } from './grading.component';

describe('GradingComponent', () => {
  let component: GradingComponent;
  let fixture: ComponentFixture<GradingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GradingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GradingComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
