import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { City, State } from '../models/city.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CityService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/cities`;

  getStates(): Observable<State[]> {
    return this.http.get<{ cities: State[] }>(`${this.baseUrl}/states`)
      .pipe(map(r => r.cities));
  }

  searchCities(stateCode: string, query: string, limit = 10): Observable<City[]> {
    const params = new HttpParams()
      .set('stateCode', stateCode)
      .set('query', query)
      .set('limit', limit);
    return this.http.get<{ cities: City[] }>(this.baseUrl, { params })
      .pipe(map(r => r.cities));
  }
}
