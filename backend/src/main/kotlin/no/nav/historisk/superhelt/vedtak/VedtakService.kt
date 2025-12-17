package no.nav.historisk.superhelt.vedtak

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.sak.SakExtensions.createVedtak
import no.nav.historisk.superhelt.sak.SakRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class VedtakService(
    private val vedtakRepository: VedtakRepository,
    private val sakRepository: SakRepository
) {
    @PreAuthorize("hasAuthority('WRITE')")
    fun fattVedtak(saksnummer: Saksnummer) {
        val sak = sakRepository.getSak(saksnummer)
        val vedtak = sak.createVedtak()

        vedtakRepository.save(vedtak)
    }


}