package no.nav.historisk.superhelt.sak

import no.nav.common.types.Belop
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.Vedtak
import java.time.Instant

object SakExtensions {
    fun Sak.getBelop(): Belop? {
        return when (this.utbetalingsType) {
            UtbetalingsType.BRUKER -> this.utbetaling?.belop
            UtbetalingsType.FORHANDSTILSAGN -> this.forhandstilsagn?.belop
            UtbetalingsType.INGEN -> null
        }

    }

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
            utbetalingsType = sak.utbetalingsType,
            belop = sak.getBelop(),
            saksbehandler = sak.saksbehandler,
            attestant = sak.attestant!!,
            soknadsDato = sak.soknadsDato!!,
            tildelingsAar = sak.tildelingsAar,
            vedtaksTidspunkt = vedtaksTidspunkt,
        )
    }

}