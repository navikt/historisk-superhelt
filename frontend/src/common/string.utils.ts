/** Strips leading zeros from the numeric part: "SH-000123" → "SH-123" */
export function kortSaksnummer(saksnummer: string): string {
    return saksnummer.replace(/^([A-Z]+-)(0*)(\d)/, "$1$3");
}

/** Returns first + last name only: "Ola Gunnar Nordmann" → "Ola Nordmann" */
export function kortNavn(navn: string): string {
    const deler = navn.trim().split(/\s+/);
    if (deler.length <= 2) return navn;
    return `${deler[0]} ${deler[deler.length - 1]}`;
}

export function enumkodeTilTekst(enumKode: string | undefined, storBokstav = true, separator = "_") {
    return enumKode
        ?.split(separator)
        .map((del, index) => {
            const lower = del.toLocaleLowerCase("nb-NO");
            return storBokstav && index === 0 ? lower.charAt(0).toLocaleUpperCase("nb-NO") + lower.slice(1) : lower;
        })
        .join(" ");
}

export function formatertValuta(belop: number | null | undefined, valuta: string = "NOK"): string {
    if (belop == null || Number.isNaN(belop)) return "Ukjent beløp";
    return belop.toLocaleString("no-NO", { style: "currency", currency: valuta , maximumFractionDigits:0});
}
