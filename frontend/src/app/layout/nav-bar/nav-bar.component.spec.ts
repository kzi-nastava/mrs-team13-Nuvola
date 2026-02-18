import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavBarComponent } from './nav-bar.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from '../../auth/services/auth.service';
import { of } from 'rxjs';

describe('NavBarComponent', () => {
  let component: NavBarComponent;
  let fixture: ComponentFixture<NavBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        NavBarComponent,
        RouterTestingModule
      ],
      providers: [
        {
          provide: AuthService,
          useValue: {
            isLoggedIn: () => true,
            getUserRole: () => 'DRIVER',
            logout: () => {}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NavBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create navbar component', () => {
    expect(component).toBeTruthy();
  });

  it('should render navigation buttons', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Ride history');
    expect(compiled.textContent).toContain('Account');
    expect(compiled.textContent).toContain('Log out');
  });
});