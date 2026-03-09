export const KR_PER_KM = 3.5;
export const MIN_PER_KM = 1.5;

export function beregnKostnad(distanceKm: number): number {
  return Math.round(distanceKm * KR_PER_KM * 100) / 100;
}

export function beregnReisetid(distanceKm: number): number {
  return Math.round(distanceKm * MIN_PER_KM);
}

export function simulerAvstand(fra: string, til: string): number {
  const seed = hashStrings(fra, til);
  return 5 + (seed % 46);
}

function hashStrings(a: string, b: string): number {
  const combined = `${a.toLowerCase().trim()}|${b.toLowerCase().trim()}`;
  let hash = 0;
  for (let i = 0; i < combined.length; i++) {
    hash = (hash << 5) - hash + combined.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}
