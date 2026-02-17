import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RegisteredUsersComponent } from './registered.users.component';

describe('RegisteredUsersComponent', () => {
  let component: RegisteredUsersComponent;
  let fixture: ComponentFixture<RegisteredUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisteredUsersComponent,
        HttpClientTestingModule
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisteredUsersComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
