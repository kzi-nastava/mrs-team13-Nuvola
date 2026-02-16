import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { ScheduledRideStartComponent } from './scheduled.ride.start.component';

describe('ScheduledRideStartComponent', () => {
  let component: ScheduledRideStartComponent;
  let fixture: ComponentFixture<ScheduledRideStartComponent>;

beforeEach(async () => {
  await TestBed.configureTestingModule({
    imports: [
      ScheduledRideStartComponent,      
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

  fixture = TestBed.createComponent(ScheduledRideStartComponent);
  component = fixture.componentInstance;
  fixture.detectChanges();
});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
