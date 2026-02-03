export type RideStatus = 'ASSIGNED' | 'CANCELED'

export interface LocationModel {
    latitude: number;
    longitude: number;
}


export interface RideModel {
    id: number,
    price: number,
    dropoff: LocationModel,
    pickup: LocationModel,
    statingTime: Date,
    driver: string,
    isFavouriteRoute: boolean

    status?: RideStatus;
    canceled?: 'DRIVER' | 'PASSENGER';
    cancelReason?: string;
}