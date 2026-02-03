import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogedinPageComponent } from './logedin.page.component';

describe('LogedinPageComponent', () => {
  let component: LogedinPageComponent;
  let fixture: ComponentFixture<LogedinPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogedinPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogedinPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
