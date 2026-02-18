export type RideStatus = 'ASSIGNED' | 'CANCELED'

export interface LocationModel {
    latitude: number;
    longitude: number;
}


export interface RideModel {
    id: number,
    price: number,
    dropoff: string,
    pickup: string,
    statingTime: Date,
    driver: string,
    isFavouriteRoute: boolean
    isPanic: boolean;

    status?: RideStatus;
    canceled?: 'DRIVER' | 'PASSENGER';
    cancelReason?: string;
}