import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;

  beforeEach(async () => {
  await TestBed.configureTestingModule({
    imports: [
      RegisterComponent,       
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

  fixture = TestBed.createComponent(RegisterComponent);
  component = fixture.componentInstance;
  fixture.detectChanges();
});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
