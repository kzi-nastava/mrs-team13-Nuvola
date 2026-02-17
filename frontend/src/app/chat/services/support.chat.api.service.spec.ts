import { TestBed } from '@angular/core/testing';

import { SupportChatApiService } from './support.chat.api.service';

describe('SupportChatApiService', () => {
  let service: SupportChatApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SupportChatApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
