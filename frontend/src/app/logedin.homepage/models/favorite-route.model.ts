import { LocationModel } from './location.model';

export interface FavoriteRoute {
  id: number;
  from: LocationModel;
  to: LocationModel;
  stops: LocationModel[];
}
