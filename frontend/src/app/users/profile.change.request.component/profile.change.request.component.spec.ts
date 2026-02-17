import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileChangeRequestComponent } from './profile.change.request.component';

describe('ProfileChangeRequestComponent', () => {
  let component: ProfileChangeRequestComponent;
  let fixture: ComponentFixture<ProfileChangeRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileChangeRequestComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProfileChangeRequestComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
