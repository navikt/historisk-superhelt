export interface TravelCostResult {
  fraStasjon: string;
  tilStasjon: string;
  distanceKm: number;
  kostnadKr: number;
  reisetidMin: number;
}

export interface TravelCostRequest {
  fraStasjon: string;
  tilStasjon: string;
}
