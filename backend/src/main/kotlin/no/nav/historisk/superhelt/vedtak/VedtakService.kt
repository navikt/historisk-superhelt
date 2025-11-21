package no.nav.historisk.superhelt.vedtak

import no.nav.historisk.superhelt.sak.SakExtensions.createVedtak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.Saksnummer
import org.springframework.stereotype.Service

@Service
class VedtakService(
    private val vedtakRepository: VedtakRepository,
    private val sakRepository: SakRepository
) {
    fun fattVedtak(saksnummer: Saksnummer) {
        val sak = sakRepository.getSak(saksnummer)
        val vedtak = sak.createVedtak()

        vedtakRepository.save(vedtak)
    }


}