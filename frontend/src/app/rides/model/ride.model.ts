export interface RideModel {
    // if needed add other fields from backend entity later
    id: number,
    price: number,
    dropoff:string,
    pickup:string,
    statingTime: Date,
    driver: string,
    isFavouriteRoute: boolean
}