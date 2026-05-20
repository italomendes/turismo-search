export interface City {
  ibgeCode: string;
  name: string;
  stateCode: string;
  stateName?: string;
}

export interface State {
  ibgeCode: string;
  name: string;
  stateCode: string;
}

export interface SearchQuery {
  type: 'location' | 'city';
  latitude?: number;
  longitude?: number;
  cityName?: string;
  stateCode?: string;
  ibgeCityCode?: string;
  radiusKm: number;
  categories: string[];
  maxResults: number;
}
