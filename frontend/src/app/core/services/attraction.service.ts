import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AttractionListResponse } from '../models/attraction.model';
import { SearchQuery } from '../models/city.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AttractionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/attractions`;

  searchByLocation(query: SearchQuery): Observable<AttractionListResponse> {
    return this.http.post<AttractionListResponse>(`${this.baseUrl}/search/by-location`, {
      latitude: query.latitude,
      longitude: query.longitude,
      radiusKm: query.radiusKm,
      categories: query.categories.length ? query.categories : undefined,
      maxResults: query.maxResults
    });
  }

  searchByCity(query: SearchQuery): Observable<AttractionListResponse> {
    return this.http.post<AttractionListResponse>(`${this.baseUrl}/search/by-city`, {
      cityName: query.cityName,
      stateCode: query.stateCode,
      ibgeCityCode: query.ibgeCityCode,
      radiusKm: query.radiusKm,
      categories: query.categories.length ? query.categories : undefined,
      maxResults: query.maxResults
    });
  }
}
