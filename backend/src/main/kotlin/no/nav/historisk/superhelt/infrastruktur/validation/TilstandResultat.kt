package no.nav.historisk.superhelt.infrastruktur.validation

data class TilstandResultat(
    val tilstand: TilstandStatus,
    val valideringsfeil: List<ValidationFieldError>,
)