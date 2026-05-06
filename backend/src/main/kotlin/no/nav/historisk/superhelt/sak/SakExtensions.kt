package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.audit.AuditLog
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.Vedtak
import java.time.Instant

object SakExtensions {

    fun Sak.createVedtak(vedtaksTidspunkt: Instant = Instant.now()): Vedtak {
        val sak = this
        SakValidator(sak)
            .checkCompleted()
            .validate()

        return Vedtak(
            saksnummer = sak.saksnummer,
            behandlingsnummer = sak.behandlingsnummer,
            stonadstype = sak.type,
            fnr = sak.fnr,
            beskrivelse = sak.beskrivelse!!,
            resultat = sak.vedtaksResultat!!,
            begrunnelse = sak.begrunnelse,
            utbetalingsType = sak.utbetalingsType ?: UtbetalingsType.INGEN,
            belop = sak.belop,
            saksbehandler = sak.saksbehandler,
            attestant = sak.attestant!!,
            soknadsDato = sak.soknadsDato!!,
            tildelingsAar = sak.tildelingsAar,
            vedtaksTidspunkt = vedtaksTidspunkt,
        )
    }

    fun Sak.auditLog(message: String) {
        AuditLog.log(
            fnr = this.fnr,
            message = message,
            customIdentifierAndValue = Pair("Saksnummer", this.saksnummer.value)
        )
    }

    fun Sak.utbetalingInfo(): String {
        return when (this.utbetalingsType) {
            UtbetalingsType.BRUKER -> "Utbetaling til bruker på ${this.belop?.value ?: "ukjent beløp"}"
            UtbetalingsType.FORHANDSTILSAGN -> "Forhåndstilsagn"
            else -> ""
        }
    }

}
