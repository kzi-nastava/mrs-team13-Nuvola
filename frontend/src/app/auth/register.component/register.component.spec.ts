import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../services/auth.service';
import { RegisterModel } from '../model/register.model';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [
        RegisterComponent,
        RouterTestingModule.withRoutes([
          { path: 'login', component: RegisterComponent }
        ])
      ],
      providers: [
        { provide: AuthService, useValue: authService },
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
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should mark invalid form as touched and not call register service', () => {
    component.submit();

    expect(component.form.invalid).toBeTrue();
    expect(component.firstName.touched).toBeTrue();
    expect(component.email.touched).toBeTrue();
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('should keep form invalid when passwords do not match', () => {
    fillValidForm();
    component.form.patchValue({ confirmPassword: 'different-password' });

    component.submit();

    expect(component.mismatch).toBeTrue();
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('should send entered registration data and navigate to login on success', () => {
    spyOn(window, 'alert');
    spyOn(router, 'navigate');
    authService.register.and.returnValue(of({}));
    fillValidForm();

    component.submit();

    const expectedData: RegisterModel = {
      email: 'petar.petrovic@example.com',
      username: 'petar.petrovic@example.com',
      password: 'secret123',
      confirmPassword: 'secret123',
      firstName: 'Petar',
      lastName: 'Petrovic',
      address: 'Fruskogorska 37',
      phone: '+381641234567',
      picture: '',
    };

    expect(authService.register).toHaveBeenCalledOnceWith(expectedData);
    expect(component.isSubmitting).toBeFalse();
    expect(component.registered).toBeTrue();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should show duplicate-account error when backend reports existing username', () => {
    spyOn(window, 'alert');
    authService.register.and.returnValue(throwError(() => ({
      status: 409,
      error: { message: 'Username already exists' },
    })));
    fillValidForm();

    component.submit();

    expect(component.isSubmitting).toBeFalse();
    expect(component.registered).toBeFalse();
    expect(component.apiError).toContain('Nalog sa ovim email/username');
    expect(window.alert).toHaveBeenCalledWith(component.apiError);
  });

  function fillValidForm() {
    component.form.patchValue({
      firstName: 'Petar',
      lastName: 'Petrovic',
      email: 'Petar.Petrovic@Example.com',
      password: 'secret123',
      confirmPassword: 'secret123',
      phone: '+381641234567',
      address: 'Fruskogorska 37',
      profileImage: null,
    });
  }
});
