package no.nav.historisk.superhelt.utbetaling

import jakarta.validation.constraints.Positive
import no.nav.common.types.NorskeKroner
import java.time.Instant
import java.util.*

data class Utbetaling(
    @field:Positive(message = "Beløp til utbetaling må være positivt")
    val belop: NorskeKroner,
    val uuid: UUID,
    val utbetalingStatus: UtbetalingStatus,
    val utbetalingTidspunkt: Instant?) {}



