package no.nav.historisk.superhelt.vedtak

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakExtensions.getBelop
import no.nav.historisk.superhelt.sak.SakValidator
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class VedtakService(private val vedtakRepository: VedtakRepository) {

    fun createVedtak(sak: Sak): Vedtak {
        SakValidator(sak)
            .checkCompleted()
            .validate()
        
        return Vedtak(
            saksnummer = sak.saksnummer,
            behandlingsnummer = sak.behandlingsnummer,
            type = sak.type,
            fnr = sak.fnr,
            tittel = sak.tittel!!,
            vedtak = sak.vedtak!!,
            begrunnelse = sak.begrunnelse,
            utbetalingsType = sak.utbetalingsType,
            belop = sak.getBelop(),
            saksbehandler = sak.saksbehandler,
            attestant = sak.saksbehandler,
            soknadsDato = sak.soknadsDato!!,
            vedtaksTidspunkt = Instant.now(),
        )
    }


}