export function isoTilLokal(isoDato?: string): string {
    if (!isoDato) { return "" }
    return new Date(isoDato).toLocaleString('no', { day: 'numeric', month: 'long', year: 'numeric' });
}

export function dateTilIsoDato(date?: Date): string | undefined {
    if (!date) { return undefined }
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

export function formatIsoDateToDayDotMonthDotYear(dateStr: string): string {
    // Validate ISO date format: YYYY-MM-DD
    const isoDateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (!isoDateRegex.test(dateStr)) {
        return "";
    }
    const [year, month, day] = dateStr.split("-");
    return `${day}.${month}.${year}`;
}