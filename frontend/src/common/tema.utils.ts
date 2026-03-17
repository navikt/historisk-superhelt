//TO DO vurder å erstatte hardkoding med kall til https://kodeverk-api.nav.no/api/v1/kodeverk/Tema/koder

const TEMA_BESKRIVELSE: Record<string, string> = {
    HJE: "Hjelpemidler",
    ORT: "Ortopediske hjelpemidler",
    HEL: "Helse",
    AAP: "Arbeidsavklaringspenger",
};

export function temaBeskrivelse(kode: string): string {
    return TEMA_BESKRIVELSE[kode] ?? kode;
}
