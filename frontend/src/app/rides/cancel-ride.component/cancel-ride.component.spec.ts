import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { CancelRideComponent } from './cancel-ride.component';

describe('CancelRideComponent', () => {
  let component: CancelRideComponent;
  let fixture: ComponentFixture<CancelRideComponent>;

beforeEach(async () => {
  await TestBed.configureTestingModule({
    imports: [
      CancelRideComponent,       
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

  fixture = TestBed.createComponent(CancelRideComponent);
  component = fixture.componentInstance;
  fixture.detectChanges();
});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
