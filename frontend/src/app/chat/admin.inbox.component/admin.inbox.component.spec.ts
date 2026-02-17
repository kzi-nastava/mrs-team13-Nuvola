import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminInboxComponent } from './admin.inbox.component';

describe('AdminInboxComponent', () => {
  let component: AdminInboxComponent;
  let fixture: ComponentFixture<AdminInboxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminInboxComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminInboxComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
