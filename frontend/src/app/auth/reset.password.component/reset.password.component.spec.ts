import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { ResetPasswordComponent } from './reset.password.component';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;

  beforeEach(async () => {
  await TestBed.configureTestingModule({
    imports: [
      ResetPasswordComponent,      
      RouterTestingModule,
      HttpClientTestingModule
    ],
    providers: [
      {
        provide: ActivatedRoute,
        useValue: {
          snapshot: {
            paramMap: { get: () => null },
            queryParamMap: { get: () => null }
          }
        }
      }
    ]
  }).compileComponents();

  fixture = TestBed.createComponent(ResetPasswordComponent);
  component = fixture.componentInstance;
  fixture.detectChanges();
});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
