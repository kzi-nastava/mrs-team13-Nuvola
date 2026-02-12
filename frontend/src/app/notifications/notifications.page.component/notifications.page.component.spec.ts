import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationsPageComponent } from './notifications.page.component';

describe('NotificationsPageComponent', () => {
  let component: NotificationsPageComponent;
  let fixture: ComponentFixture<NotificationsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationsPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotificationsPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
