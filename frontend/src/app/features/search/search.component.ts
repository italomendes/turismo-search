import { Component, inject, signal, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GeolocationService } from '../../core/services/geolocation.service';
import { AttractionService } from '../../core/services/attraction.service';
import { AttractionListResponse } from '../../core/models/attraction.model';
import { SearchQuery } from '../../core/models/city.model';
import { CityAutocompleteComponent } from './components/city-autocomplete/city-autocomplete.component';
import { FilterPanelComponent } from './components/filter-panel/filter-panel.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, FormsModule, CityAutocompleteComponent, FilterPanelComponent],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent {
  private geo = inject(GeolocationService);
  private attractionService = inject(AttractionService);

  searchMode = signal<'location' | 'city'>('city');
  loading = signal(false);
  error = signal<string | null>(null);
  geoError = signal<string | null>(null);

  radiusKm = 50;
  maxResults = 20;
  selectedCategories: string[] = [];
  selectedCity: { name: string; stateCode: string; ibgeCode?: string } | null = null;

  results = output<AttractionListResponse>();

  setMode(mode: 'location' | 'city') {
    this.searchMode.set(mode);
    this.geoError.set(null);
  }

  onCitySelected(city: { name: string; stateCode: string; ibgeCode?: string }) {
    this.selectedCity = city;
  }

  onCategoriesChanged(cats: string[]) {
    this.selectedCategories = cats;
  }

  search() {
    if (this.searchMode() === 'location') {
      this.searchByGPS();
    } else {
      this.searchByCity();
    }
  }

  private searchByGPS() {
    this.loading.set(true);
    this.error.set(null);
    this.geoError.set(null);

    this.geo.getCurrentPosition().subscribe({
      next: (pos) => {
        const query: SearchQuery = {
          type: 'location',
          latitude: pos.latitude,
          longitude: pos.longitude,
          radiusKm: this.radiusKm,
          categories: this.selectedCategories,
          maxResults: this.maxResults
        };
        this.attractionService.searchByLocation(query).subscribe({
          next: (res) => { this.results.emit(res); this.loading.set(false); },
          error: (e) => { this.error.set('Erro ao buscar atrações.'); this.loading.set(false); }
        });
      },
      error: (e) => {
        this.geoError.set('Não foi possível obter sua localização. ' + e);
        this.loading.set(false);
      }
    });
  }

  private searchByCity() {
    if (!this.selectedCity) {
      this.error.set('Selecione uma cidade.');
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const query: SearchQuery = {
      type: 'city',
      cityName: this.selectedCity.name,
      stateCode: this.selectedCity.stateCode,
      ibgeCityCode: this.selectedCity.ibgeCode,
      radiusKm: this.radiusKm,
      categories: this.selectedCategories,
      maxResults: this.maxResults
    };

    this.attractionService.searchByCity(query).subscribe({
      next: (res) => { this.results.emit(res); this.loading.set(false); },
      error: () => { this.error.set('Erro ao buscar atrações.'); this.loading.set(false); }
    });
  }
}
