import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ChangeDetectorRef } from '@angular/core';

import { GradingComponent } from './grading.component';
import { AuthService } from '../../auth/services/auth.service';

fdescribe('GradingComponent', () => {
  let component: GradingComponent;
  let fixture: ComponentFixture<GradingComponent>;
  let httpMock: HttpTestingController;

  const REVIEWS_URL = 'http://localhost:8080/api/reviews';

  function setupWithRideIdParam(rideIdParam: any) {
    const authSpy = jasmine.createSpyObj<AuthService>('AuthService', [
      'getUsername',
    ]);

    TestBed.configureTestingModule({
      imports: [GradingComponent, HttpClientTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'rideId' ? rideIdParam : null),
              },
            },
          },
        },
        { provide: AuthService, useValue: authSpy },
        
        //{ provide: ChangeDetectorRef, useValue: { detectChanges: () => {} } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GradingComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);

    // trigger ngOnInit
    fixture.detectChanges();

    return { authSpy };
  }

  afterEach(() => {
    if (httpMock) httpMock.verify();
  });

  it('should create', async () => {
    setupWithRideIdParam('10');
    expect(component).toBeTruthy();
  });

  it('ngOnInit should parse rideId from route param', async () => {
    setupWithRideIdParam('123');
    expect(component.rideId).toBe(123);
    expect(component.errorMessage).toBeNull();
  });

  it('ngOnInit should set errorMessage when rideId is invalid (null/NaN/0)', async () => {
    setupWithRideIdParam(null);
    expect(component.rideId).toBe(0);
    expect(component.errorMessage).toBe('Invalid rideId in URL.');
  });

  it('submit should show error when form is invalid and mark controls as touched', async () => {
    setupWithRideIdParam('5');

    // rating filds are empty
    component.submit();

    expect(component.errorMessage).toBe('Fill mandatory fields (ratings 1–5).');
    expect(component.successMessage).toBeNull();

    const v = component.form.get('vehicleRating')!;
    const d = component.form.get('driverRating')!;
    expect(v.touched).toBeTrue();
    expect(d.touched).toBeTrue();
  });

  it('submit should show error if user is not logged in (no username)', async () => {
    const { authSpy } = setupWithRideIdParam('5');
    authSpy.getUsername.and.returnValue('');

    component.form.patchValue({ vehicleRating: 5, driverRating: 4, comment: '' });
    expect(component.form.valid).toBeTrue();

    component.submit();

    expect(component.errorMessage).toBe('You must be logged in to submit a rating.');
    expect(component.isSending).toBeFalse();

    httpMock.expectNone(REVIEWS_URL);
  });

  it('submit should POST payload and on success set successMessage + reset form', async () => {
    const { authSpy } = setupWithRideIdParam('77');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({
      vehicleRating: 5,
      driverRating: 3,
      comment: '  super voznja  ',
    });
    component.submit();

    expect(component.isSending).toBeTrue();

    const req = httpMock.expectOne(REVIEWS_URL);
    expect(req.request.method).toBe('POST');

    expect(req.request.body).toEqual({
      vehicleRating: 5,
      driverRating: 3,
      comment: 'super voznja', // trim
      rideId: 77,
      username: 'marko',
    });

    // simulate success
    req.flush({});

    expect(component.isSending).toBeFalse();
    expect(component.errorMessage).toBeNull();
    expect(component.successMessage).toBe('Rating submitted successfully.');

    // reset form
    expect(component.form.value.vehicleRating).toBeNull();
    expect(component.form.value.driverRating).toBeNull();
    expect(component.form.value.comment).toBeNull();
  });

  
  it('should disable submit button while sending', () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 5, driverRating: 5, comment: '' });

    component.submit();
    fixture.detectChanges();

    const btn: HTMLButtonElement =
      fixture.nativeElement.querySelector('button[type="submit"]');
    expect(component.isSending).toBeTrue();
    expect(btn.disabled).toBeTrue();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush({});
    fixture.detectChanges();

    expect(component.isSending).toBeFalse();
    expect(btn.disabled).toBeFalse();
  });

  it('should map error status 0 to network/CORS message', async () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 4, driverRating: 4, comment: '' });
    component.submit();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush(null, { status: 0, statusText: 'Unknown Error' });

    expect(component.isSending).toBeFalse();
    expect(component.errorMessage).toBe('Cannot connect to server (network/CORS).');
  });

  it('should map error status 400 and use backend string if provided', async () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 4, driverRating: 4, comment: '' });
    component.submit();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush('VALIDATION_FAILED', { status: 400, statusText: 'Bad Request' });

    expect(component.errorMessage).toBe('VALIDATION_FAILED');
  });

  it('should map error status 401', async () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 4, driverRating: 4, comment: '' });
    component.submit();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush({ message: 'nope' }, { status: 401, statusText: 'Unauthorized' });

    expect(component.errorMessage).toBe('You do not have permission to send a review.');
  });

  it('should map error status 403', async () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 4, driverRating: 4, comment: '' });
    component.submit();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush({}, { status: 403, statusText: 'Forbidden' });

    expect(component.errorMessage).toBe('You cant rate this ride (time limit is 3 days).');
  });

  it('should map error status 409', async () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 4, driverRating: 4, comment: '' });
    component.submit();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush({}, { status: 409, statusText: 'Conflict' });

    expect(component.errorMessage).toBe('You have already submitted a review for this ride.');
  });

  it('should map other errors to backend message if present', async () => {
    const { authSpy } = setupWithRideIdParam('10');
    authSpy.getUsername.and.returnValue('marko');

    component.form.patchValue({ vehicleRating: 4, driverRating: 4, comment: '' });
    component.submit();

    const req = httpMock.expectOne(REVIEWS_URL);
    req.flush({ message: 'SERVER_FAIL' }, { status: 500, statusText: 'Server Error' });

    expect(component.errorMessage).toBe('SERVER_FAIL');
  });

  it('hasError should return true only when touched + has error', async () => {
    setupWithRideIdParam('10');

    const c = component.form.get('vehicleRating')!;
    c.setValue(null);
    c.markAsTouched();
    c.updateValueAndValidity();

    expect(component.hasError('vehicleRating', 'required')).toBeTrue();

    // if not touched - false
    const d = component.form.get('driverRating')!;
    d.setValue(null);
    d.markAsUntouched();
    d.updateValueAndValidity();

    expect(component.hasError('driverRating', 'required')).toBeFalse();
  });

  it('should enforce min/max validators (1-5)', async () => {
    setupWithRideIdParam('10');

    component.form.get('vehicleRating')!.setValue(0);
    component.form.get('driverRating')!.setValue(6);

    expect(component.form.valid).toBeFalse();
    expect(component.form.get('vehicleRating')!.hasError('min')).toBeTrue();
    expect(component.form.get('driverRating')!.hasError('max')).toBeTrue();
  });
});