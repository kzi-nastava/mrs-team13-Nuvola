import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserChatPage } from './user.chat.page';

describe('UserChatPage', () => {
  let component: UserChatPage;
  let fixture: ComponentFixture<UserChatPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserChatPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserChatPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
