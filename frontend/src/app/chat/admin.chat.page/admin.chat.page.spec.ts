import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminChatPage } from './admin.chat.page';

describe('AdminChatPage', () => {
  let component: AdminChatPage;
  let fixture: ComponentFixture<AdminChatPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminChatPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminChatPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
