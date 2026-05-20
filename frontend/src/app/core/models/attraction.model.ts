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
  WATERFALL: '#00E5FF',
  TRAIL: '#69FF47',
  PARK: '#00C853',
  BEACH: '#FFD600',
  HISTORICAL: '#FF6D00',
  CULTURAL: '#D500F9',
  GASTRONOMIC: '#FF4D4D',
  INN: '#FF9100',
  CAVE: '#795548',
  ADVENTURE: '#E040FB',
  RELIGIOUS: '#607D8B',
  OTHER: '#9E9E9E'
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
