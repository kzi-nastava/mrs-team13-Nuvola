import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DriverPasswordComponent } from './driver.password.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';

describe('DriverPasswordComponent', () => {
  let component: DriverPasswordComponent;
  let fixture: ComponentFixture<DriverPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DriverPasswordComponent,
        RouterTestingModule
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: { get: () => 'mock-token' },
              paramMap: { get: () => null }
            }
          }
        },
        {
          provide: AuthService,
          useValue: {
            activateAccount: () => of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DriverPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});