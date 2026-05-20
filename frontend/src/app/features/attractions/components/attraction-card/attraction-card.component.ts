import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Attraction, CATEGORY_COLORS } from '../../../../core/models/attraction.model';

@Component({
  selector: 'app-attraction-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attraction-card.component.html',
  styleUrl: './attraction-card.component.scss'
})
export class AttractionCardComponent {
  attraction = input.required<Attraction>();
  selected = input<boolean>(false);
  cardClicked = output<string>();

  getCategoryColor(): string {
    const cat = this.attraction().category;
    return cat ? CATEGORY_COLORS[cat] : '#717171';
  }

  getCategoryColorLight(): string {
    const color = this.getCategoryColor();
    const r = parseInt(color.slice(1, 3), 16);
    const g = parseInt(color.slice(3, 5), 16);
    const b = parseInt(color.slice(5, 7), 16);
    return `rgba(${r},${g},${b},0.15)`;
  }

  getCategoryBorderColor(): string {
    const color = this.getCategoryColor();
    const r = parseInt(color.slice(1, 3), 16);
    const g = parseInt(color.slice(3, 5), 16);
    const b = parseInt(color.slice(5, 7), 16);
    return `rgba(${r},${g},${b},0.35)`;
  }

  getConfidenceLabel(): string {
    const score = this.attraction().aiConfidenceScore;
    if (score >= 0.9) return 'OSM verified';
    if (score >= 0.7) return 'AI confident';
    return 'AI estimated';
  }
}
