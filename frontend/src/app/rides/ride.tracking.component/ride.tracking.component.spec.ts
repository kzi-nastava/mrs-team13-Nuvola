import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { RideTrackingComponent } from './ride.tracking.component';

describe('RideTrackingComponent', () => {
  let component: RideTrackingComponent;
  let fixture: ComponentFixture<RideTrackingComponent>;

 beforeEach(async () => {
  await TestBed.configureTestingModule({
    imports: [
      RideTrackingComponent,       
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

  fixture = TestBed.createComponent(RideTrackingComponent);
  component = fixture.componentInstance;
  spyOn(component as any, 'initMap');
  fixture.detectChanges();
});

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
