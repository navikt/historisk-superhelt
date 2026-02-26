package no.nav.historisk.superhelt.sak.rest

import jakarta.validation.constraints.Size
import no.nav.common.types.Aar
import no.nav.common.types.Belop
import no.nav.historisk.superhelt.brev.BrevId
import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import java.time.LocalDate


data class SakUpdateRequestDto(
    val type: StonadsType? = null,
    @field:Size(max = 200)
    val beskrivelse: String? = null,
    @field:Size(max = 1000)
    val begrunnelse: String? = null,
    val soknadsDato: LocalDate? = null,
    val tildelingsAar: Aar? = null,
    val vedtaksResultat: VedtaksResultat? = null,
    val utbetalingsType: UtbetalingsType? = null,
    val belop: Belop? = null,
)

data class AttesterSakRequestDto(
    val godkjent: Boolean,
    @field:Size(min = 5, max = 500)
    val kommentar: String? = null,
)

data class FeilregisterRequestDto(
    @field:Size(min = 5, max = 1000)
    val aarsak: String,
)

data class HenlagtSakRequestDto(
    @field:Size(min = 5, max = 1000)
    val aarsak: String,
    val henleggelseBrevId: BrevId
)

data class GjenapneSakRequestDto(
    @field:Size(min = 5, max = 1000)
    val aarsak: String,
)

enum class AggregertSakStatus {
    OK,
    FEILET,
}

data class SakStatusDto(
    val sakStatus: SakStatus,
    val utbetalingStatus: UtbetalingStatus?,
    val brevStatus: BrevStatus?,
    val aggregertStatus: AggregertSakStatus,
)



