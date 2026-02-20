export function enumkodeTilTekst(enumKode: string | undefined, storBokstav = true, separator = "_") {
    return enumKode
        ?.split(separator)
        .map((del, index) => {
            const lower = del.toLocaleLowerCase();
            return storBokstav && index === 0 ? lower.charAt(0).toLocaleUpperCase() + lower.slice(1) : lower;
        })
        .join(" ");
}
