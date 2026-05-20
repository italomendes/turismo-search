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
    return cat ? CATEGORY_COLORS[cat] : '#FFD600';
  }

  getConfidenceLabel(): string {
    const score = this.attraction().aiConfidenceScore;
    if (score >= 0.9) return '⭐ Alta confiança';
    if (score >= 0.7) return '✓ Boa confiança';
    return '~ Confiança moderada';
  }
}
