import {
  Component, OnInit, OnDestroy, input, effect,
  output, ElementRef, viewChild, Injector, inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { Attraction, CATEGORY_COLORS } from '../../core/models/attraction.model';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements OnInit, OnDestroy {
  mapEl = viewChild<ElementRef>('mapContainer');

  attractions = input<Attraction[]>([]);
  selectedId = input<string | null>(null);
  attractionClicked = output<string>();

  private map?: L.Map;
  private markers = new Map<string, L.Marker>();
  private injector = inject(Injector);

  ngOnInit() {
    this.initMap();

    // effect() precisa de contexto de injeção — usar Injector explícito
    effect(() => {
      const list = this.attractions();
      this.clearMarkers();
      list.forEach(a => this.addMarker(a));
      this.fitBounds();
    }, { injector: this.injector });

    effect(() => {
      const id = this.selectedId();
      if (id) this.highlightMarker(id);
    }, { injector: this.injector });
  }

  private initMap() {
    setTimeout(() => {
      const el = this.mapEl()?.nativeElement;
      if (!el) return;

      this.map = L.map(el, {
        center: [-15.78, -47.93],
        zoom: 5,
        zoomControl: true
      });

      L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
        attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> © <a href="https://carto.com/">CARTO</a>',
        maxZoom: 19
      }).addTo(this.map);
    }, 50);
  }

  private addMarker(attraction: Attraction) {
    if (!attraction.coordinates || !this.map) return;

    const color = attraction.category ? CATEGORY_COLORS[attraction.category] : '#FFD600';

    const icon = L.divIcon({
      className: '',
      html: `<div class="neo-pin" style="background:${color}">
               <span>${this.getCategoryEmoji(attraction.category)}</span>
             </div>`,
      iconSize: [40, 40],
      iconAnchor: [20, 40],
      popupAnchor: [0, -42]
    });

    const marker = L.marker(
      [attraction.coordinates.latitude, attraction.coordinates.longitude],
      { icon }
    );

    marker.bindPopup(`
      <div class="map-popup">
        <strong>${attraction.name}</strong>
        <span class="popup-category">${attraction.categoryDisplayName}</span>
        ${attraction.distanceKm ? `<span>${attraction.distanceKm} km</span>` : ''}
      </div>
    `, { className: 'neo-popup' });

    marker.on('click', () => this.attractionClicked.emit(attraction.id));
    marker.addTo(this.map);
    this.markers.set(attraction.id, marker);
  }

  private clearMarkers() {
    this.markers.forEach(m => this.map?.removeLayer(m));
    this.markers.clear();
  }

  private fitBounds() {
    if (!this.map || this.markers.size === 0) return;
    const coords: L.LatLngExpression[] = [];
    this.markers.forEach((m) => coords.push(m.getLatLng()));
    this.map.fitBounds(L.latLngBounds(coords), { padding: [40, 40] });
  }

  private highlightMarker(id: string) {
    const marker = this.markers.get(id);
    if (marker) {
      marker.openPopup();
      this.map?.panTo(marker.getLatLng());
    }
  }

  private getCategoryEmoji(category: string): string {
    const map: Record<string, string> = {
      WATERFALL: '💧', TRAIL: '🥾', PARK: '🌳', BEACH: '🏖️',
      HISTORICAL: '🏛️', CULTURAL: '🎭', GASTRONOMIC: '🍽️',
      INN: '🏡', CAVE: '🕳️', ADVENTURE: '🧗', RELIGIOUS: '⛪', OTHER: '📍'
    };
    return map[category] ?? '📍';
  }

  ngOnDestroy() {
    this.map?.remove();
  }
}
