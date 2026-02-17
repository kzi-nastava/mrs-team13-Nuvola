import { TestBed } from '@angular/core/testing';

import { SupportChatWsService } from './support.chat.ws.service';

describe('SupportChatWsService', () => {
  let service: SupportChatWsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SupportChatWsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
