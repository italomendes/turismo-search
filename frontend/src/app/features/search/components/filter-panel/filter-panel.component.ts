import { Component, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CATEGORY_LABELS } from '../../../../core/models/attraction.model';

@Component({
  selector: 'app-filter-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filter-panel.component.html',
  styleUrl: './filter-panel.component.scss'
})
export class FilterPanelComponent {
  radiusKm = input<number>(50);
  radiusChanged = output<number>();
  categoriesChanged = output<string[]>();

  expanded = signal(false);
  selectedCategories = new Set<string>();
  radius = 50;

  readonly categoryEntries = Object.entries(CATEGORY_LABELS);

  toggleCategory(key: string) {
    if (this.selectedCategories.has(key)) {
      this.selectedCategories.delete(key);
    } else {
      this.selectedCategories.add(key);
    }
    this.categoriesChanged.emit([...this.selectedCategories]);
  }

  onRadiusChange() {
    this.radiusChanged.emit(this.radius);
  }

  isSelected(key: string) {
    return this.selectedCategories.has(key);
  }
}
