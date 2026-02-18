import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';

import { RegisterDriversComponent } from './register.drivers.component';
import { DriverService } from '../../services/driver.service';

fdescribe('RegisterDriversComponent - Full Validation Suite', () => {

  let component: RegisterDriversComponent;
  let fixture: ComponentFixture<RegisterDriversComponent>;
  let driverServiceSpy: jasmine.SpyObj<DriverService>;

  const validFormData = {
    firstName: 'Marko',
    lastName: 'Markovic',
    email: 'marko@gmail.com',
    phone: '+381641234567',
    address: 'Fruskogorska 12',
    picture: null,
    model: 'Audi A4',
    type: 'STANDARD',
    regNumber: 'BG-123-RV',
    numOfSeats: 4,
    babyFriendly: false,
    petFriendly: false
  };

  beforeEach(async () => {

    driverServiceSpy = jasmine.createSpyObj('DriverService', [
      'createDriver',
      'uploadDriverPicture'
    ]);

    driverServiceSpy.createDriver.and.returnValue(of({ id: 1 }));
    driverServiceSpy.uploadDriverPicture.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [RegisterDriversComponent, ReactiveFormsModule],
      providers: [{ provide: DriverService, useValue: driverServiceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterDriversComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid when form is empty', () => {
    component.driverForm.reset();
    expect(component.driverForm.invalid).toBeTrue();
  });

  it('should be valid when form is filled correctly', () => {
    component.driverForm.setValue(validFormData);
    expect(component.driverForm.valid).toBeTrue();
  });


  // field validation

  it('should invalidate wrong first name format', () => {
    component.driverForm.patchValue({ ...validFormData, firstName: 'marko123' });
    expect(component.driverForm.get('firstName')?.invalid).toBeTrue();
  });

  it('should invalidate wrong last name format', () => {
    component.driverForm.patchValue({ ...validFormData, lastName: 'markovic123' });
    expect(component.driverForm.get('lastName')?.invalid).toBeTrue();
});

  it('should invalidate wrong email format', () => {
    component.driverForm.patchValue({ ...validFormData, email: 'invalid' });
    expect(component.driverForm.get('email')?.invalid).toBeTrue();
  });

  it('should invalidate wrong phone format', () => {
    component.driverForm.patchValue({ ...validFormData, phone: 'abc123' });
    expect(component.driverForm.get('phone')?.invalid).toBeTrue();
  });

  it('should invalidate wrong address format', () => {
    component.driverForm.patchValue({ ...validFormData, address: '1' });
    expect(component.driverForm.get('address')?.invalid).toBeTrue();
  });

  it('should invalidate wrong vehicle model format', () => {
    component.driverForm.patchValue({ ...validFormData, model: 'A' });
    expect(component.driverForm.get('model')?.invalid).toBeTrue();
  });

  it('should invalidate wrong regNumber format', () => {
    component.driverForm.patchValue({ ...validFormData, regNumber: '12345' });
    expect(component.driverForm.get('regNumber')?.invalid).toBeTrue();
  });

  it('should invalidate seats less than 4', () => {
    component.driverForm.patchValue({ ...validFormData, numOfSeats: 3 });
    expect(component.driverForm.get('numOfSeats')?.invalid).toBeTrue();
  });

  it('should invalidate seats more than 10', () => {
    component.driverForm.patchValue({ ...validFormData, numOfSeats: 15 });
    expect(component.driverForm.get('numOfSeats')?.invalid).toBeTrue();
  });

  // exactly 4 seats
  it('should accept exactly 4 seats as valid', () => {
      component.driverForm.patchValue({ ...validFormData, numOfSeats: 4 });
      expect(component.driverForm.get('numOfSeats')?.valid).toBeTrue();
  });

  // exactly 10 seats
  it('should accept exactly 10 seats as valid', () => {
      component.driverForm.patchValue({ ...validFormData, numOfSeats: 10 });
      expect(component.driverForm.get('numOfSeats')?.valid).toBeTrue();
  });

  // submit

  it('should call createDriver on valid submit', () => {
    component.driverForm.setValue(validFormData);
    component.onSubmit();

    expect(driverServiceSpy.createDriver).toHaveBeenCalled();
  });

  it('should NOT call service when form invalid', () => {
    component.driverForm.reset();
    component.onSubmit();

    expect(driverServiceSpy.createDriver).not.toHaveBeenCalled();
  });

  

  it('should show success toast after successful registration', () => {
    component.driverForm.setValue(validFormData);
    component.onSubmit();

    expect(component.successToast).toBeTrue();
  });

  it('should reset form after successful registration', () => {
    component.driverForm.setValue(validFormData);
    component.onSubmit();

    expect(component.driverForm.get('firstName')?.value).toBe('');
    expect(component.driverForm.get('type')?.value).toBe('STANDARD');
    expect(component.profilePreview).toBeNull();
  });

  // picture upload

  it('should call uploadDriverPicture when file is selected', () => {
    component.driverForm.setValue(validFormData);

    const mockFile = new File(['dummy'], 'test.png', { type: 'image/png' });

    const input = document.createElement('input');
    Object.defineProperty(input, 'files', {
      value: [mockFile]
    });

    component.fileInput = { nativeElement: input } as any;

    component.onSubmit();

    expect(driverServiceSpy.uploadDriverPicture).toHaveBeenCalled();
  });

  it('should ignore non-image file upload', () => {
    const mockFile = new File(['dummy'], 'test.txt', { type: 'text/plain' });

    const event = {
      target: { files: [mockFile] }
    };

    component.onFileSelected(event);

    expect(component.profilePreview).toBeNull();
  });

  // error handling

  it('should set email error when EMAIL_ALREADY_EXISTS returned', () => {
    component.driverForm.setValue(validFormData);

    driverServiceSpy.createDriver.and.returnValue(
      throwError(() => ({ error: { code: 'EMAIL_ALREADY_EXISTS' } }))
    );

    component.onSubmit();

    expect(component.driverForm.get('email')?.hasError('alreadyExists')).toBeTrue();
  });

  it('should set regNumber error when REG_NUMBER_ALREADY_EXISTS returned', () => {
    component.driverForm.setValue(validFormData);

    driverServiceSpy.createDriver.and.returnValue(
      throwError(() => ({ error: { code: 'REG_NUMBER_ALREADY_EXISTS' } }))
    );

    component.onSubmit();

    expect(component.driverForm.get('regNumber')?.hasError('alreadyExists')).toBeTrue();
  });


  it('should clear email alreadyExists error on value change', () => {
    component.driverForm.get('email')?.setErrors({ alreadyExists: true });
    component.driverForm.get('email')?.setValue('new@gmail.com');

    expect(component.driverForm.get('email')?.hasError('alreadyExists')).toBeFalse();
  });

  it('should clear regNumber alreadyExists error on value change', () => {
    component.driverForm.get('regNumber')?.setErrors({ alreadyExists: true });
    component.driverForm.get('regNumber')?.setValue('BG-999-AA');

    expect(component.driverForm.get('regNumber')?.hasError('alreadyExists')).toBeFalse();
  });

});