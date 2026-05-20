import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchComponent } from './features/search/search.component';
import { MapComponent } from './features/map/map.component';
import { AttractionCardComponent } from './features/attractions/components/attraction-card/attraction-card.component';
import { Attraction, AttractionListResponse } from './core/models/attraction.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, SearchComponent, MapComponent, AttractionCardComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  results = signal<AttractionListResponse | null>(null);
  selectedId = signal<string | null>(null);

  onResults(res: AttractionListResponse) {
    this.results.set(res);
    this.selectedId.set(null);
  }

  onMapClick(id: string) {
    this.selectedId.set(id);
    const el = document.getElementById('card-' + id);
    el?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  onCardClick(id: string) {
    this.selectedId.set(id);
  }

  get attractions(): Attraction[] {
    return this.results()?.attractions ?? [];
  }
}
