export interface Coordinates {
  latitude: number;
  longitude: number;
}

export type AttractionCategory =
  | 'WATERFALL' | 'TRAIL' | 'PARK' | 'BEACH'
  | 'HISTORICAL' | 'CULTURAL' | 'GASTRONOMIC'
  | 'INN' | 'CAVE' | 'ADVENTURE' | 'RELIGIOUS' | 'OTHER';

export const CATEGORY_LABELS: Record<AttractionCategory, string> = {
  WATERFALL: 'Cachoeira',
  TRAIL: 'Trilha',
  PARK: 'Parque / Área Natural',
  BEACH: 'Praia / Lago',
  HISTORICAL: 'Patrimônio Histórico',
  CULTURAL: 'Cultura / Arte',
  GASTRONOMIC: 'Gastronomia',
  INN: 'Pousada',
  CAVE: 'Gruta / Caverna',
  ADVENTURE: 'Aventura',
  RELIGIOUS: 'Religioso',
  OTHER: 'Outros'
};

export const CATEGORY_COLORS: Record<AttractionCategory, string> = {
  WATERFALL: '#0077B6',
  TRAIL: '#2D6A4F',
  PARK: '#40916C',
  BEACH: '#F4A261',
  HISTORICAL: '#BC6C25',
  CULTURAL: '#7B2D8B',
  GASTRONOMIC: '#E63946',
  INN: '#E07B39',
  CAVE: '#6D4C41',
  ADVENTURE: '#9C27B0',
  RELIGIOUS: '#546E7A',
  OTHER: '#717171'
};

export interface Attraction {
  id: string;
  name: string;
  description: string;
  category: AttractionCategory;
  categoryDisplayName: string;
  subcategory?: string;
  coordinates?: Coordinates;
  distanceKm?: number;
  tags: string[];
  highlights: string[];
  bestPeriod?: string;
  openingHours?: string;
  entryFee?: string;
  accessibilityInfo?: string;
  tips: string[];
  address?: string;
  aiConfidenceScore: number;
}

export interface AttractionListResponse {
  city?: string;
  state?: string;
  totalFound: number;
  attractions: Attraction[];
}
