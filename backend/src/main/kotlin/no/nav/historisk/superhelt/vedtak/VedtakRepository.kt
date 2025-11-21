package no.nav.historisk.superhelt.vedtak

import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.vedtak.db.VedtakJpaEntity
import no.nav.historisk.superhelt.vedtak.db.VedtakJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class VedtakRepository(
    private val vedtakJpaRepository: VedtakJpaRepository,
    private val sakRepository: SakRepository
) {
    @Transactional
    internal fun save(vedtak: Vedtak) {
        val sakEntity = sakRepository.getSakEntityOrThrow(vedtak.saksnummer)
        val vedtakJpaEntity = VedtakJpaEntity(
            sak = sakEntity,
            behandlingsnummer = vedtak.behandlingsnummer,
            type = vedtak.type,
            fnr = vedtak.fnr,
            tittel = vedtak.tittel,
            vedtak = vedtak.vedtak,
            begrunnelse = vedtak.begrunnelse,
            utbetalingsType = vedtak.utbetalingsType,
            belop = vedtak.belop?.value,
            saksbehandler = vedtak.saksbehandler,
            attestant = vedtak.attestant,
            soknadsDato = vedtak.soknadsDato,
            vedtaksTidspunkt = vedtak.vedtaksTidspunkt,
        )
        vedtakJpaRepository.save(vedtakJpaEntity)
    }

    @Transactional(readOnly = true)
    fun findBySak(saksnummer: Saksnummer): List<Vedtak> {
        return vedtakJpaRepository.findBySak_Id(saksnummer.id).map { it.toDomain() }
    }


}
