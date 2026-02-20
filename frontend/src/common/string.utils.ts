export function enumkodeTilTekst(enumKode: string | undefined, storBokstav = true, separator = "_") {
    return enumKode
        ?.split(separator)
        .map((del, index) => {
            const lower = del.toLocaleLowerCase("nb-NO");
            return storBokstav && index === 0 ? lower.charAt(0).toLocaleUpperCase("nb-NO") + lower.slice(1) : lower;
        })
        .join(" ");
}
