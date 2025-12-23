import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccountComponent } from './account.component';

describe('AccountComponent', () => {
  let component: AccountComponent;
  let fixture: ComponentFixture<AccountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountComponent]
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
    component.onSave();
    expect(component.successMessage).toBe('Changes saved!');
  });
});
