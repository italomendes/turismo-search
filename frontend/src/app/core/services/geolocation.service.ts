import { Injectable } from '@angular/core';
import { Observable, Observer } from 'rxjs';

export interface GeoPosition {
  latitude: number;
  longitude: number;
}

@Injectable({ providedIn: 'root' })
export class GeolocationService {
  getCurrentPosition(): Observable<GeoPosition> {
    return new Observable((observer: Observer<GeoPosition>) => {
      if (!navigator.geolocation) {
        observer.error('Geolocalização não suportada neste navegador.');
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          observer.next({
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude
          });
          observer.complete();
        },
        (err) => observer.error(err.message),
        { timeout: 10000, enableHighAccuracy: true }
      );
    });
  }

  isSupported(): boolean {
    return 'geolocation' in navigator;
  }
}
