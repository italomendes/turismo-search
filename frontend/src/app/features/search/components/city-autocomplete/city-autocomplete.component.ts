import { Component, inject, signal, output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { CityService } from '../../../../core/services/city.service';
import { City, State } from '../../../../core/models/city.model';

@Component({
  selector: 'app-city-autocomplete',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './city-autocomplete.component.html',
  styleUrl: './city-autocomplete.component.scss'
})
export class CityAutocompleteComponent implements OnInit {
  private cityService = inject(CityService);

  citySelected = output<{ name: string; stateCode: string; ibgeCode?: string }>();

  states = signal<State[]>([]);
  cities = signal<City[]>([]);
  selectedState = '';
  cityQuery = '';
  showDropdown = signal(false);

  private search$ = new Subject<string>();

  ngOnInit() {
    this.cityService.getStates().subscribe(s => this.states.set(s));

    this.search$.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(q => this.cityService.searchCities(this.selectedState, q, 10))
    ).subscribe(cities => {
      this.cities.set(cities);
      this.showDropdown.set(cities.length > 0);
    });
  }

  onCityInput() {
    if (this.selectedState && this.cityQuery.length >= 2) {
      this.search$.next(this.cityQuery);
    } else {
      this.cities.set([]);
      this.showDropdown.set(false);
    }
  }

  selectCity(city: City) {
    this.cityQuery = city.name;
    this.showDropdown.set(false);
    this.citySelected.emit({ name: city.name, stateCode: city.stateCode, ibgeCode: city.ibgeCode });
  }
}
