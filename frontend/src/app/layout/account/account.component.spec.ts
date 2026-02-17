import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccountComponent } from './account.component';
import { AccountService } from '../../services/account.service';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

describe('AccountComponent', () => {
  let component: AccountComponent;
  let fixture: ComponentFixture<AccountComponent>;

  const mockAccountService = {
    getProfile: () => of({
      firstName: 'Test',
      lastName: 'User',
      email: 'test@test.com',
      phone: '123456',
      address: 'Test Street',
      picture: null
    }),
    updateProfile: () => of({}),
    uploadPicture: () => of({ picture: 'test.jpg' })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AccountComponent,
        RouterTestingModule
      ],
      providers: [
        { provide: AccountService, useValue: mockAccountService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create account component', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid when form is empty', () => {
    component.accountForm.reset();
    expect(component.accountForm.invalid).toBe(true);
  });

  it('should show success message when form is valid and saved', () => {
    component.accountForm.patchValue({
      firstName: 'Test',
      lastName: 'User',
      email: 'test@test.com',
      phone: '123456',
      address: 'Test Street'
    });

    component.onSave();

    expect(component.successMessage).toBe('Changes saved successfully!');
  });
});