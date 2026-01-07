package no.nav.historisk.superhelt.vedtak

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.vedtak.db.VedtakJpaEntity
import no.nav.historisk.superhelt.vedtak.db.VedtakJpaRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class VedtakRepository(
    private val vedtakJpaRepository: VedtakJpaRepository,
    private val sakRepository: SakRepository
) {

    @PreAuthorize("hasAuthority('WRITE')")
    internal fun save(vedtak: Vedtak) {
        val sakEntity = sakRepository.getSakEntityOrThrow(vedtak.saksnummer)
        val vedtakJpaEntity = VedtakJpaEntity(
            sak = sakEntity,
            behandlingsnummer = vedtak.behandlingsnummer,
            type = vedtak.stonadstype,
            fnr = vedtak.fnr,
            beskrivelse = vedtak.beskrivelse,
            resultat = vedtak.resultat,
            begrunnelse = vedtak.begrunnelse,
            utbetalingsType = vedtak.utbetalingsType,
            belop = vedtak.belop?.value,
            saksbehandler = vedtak.saksbehandler,
            attestant = vedtak.attestant,
            soknadsDato = vedtak.soknadsDato,
            vedtaksTidspunkt = vedtak.vedtaksTidspunkt,
            tildelingsAar = vedtak.tildelingsAar?.value,
        )
        vedtakJpaRepository.save(vedtakJpaEntity)
    }

    @Transactional(readOnly = true)
    fun findBySak(saksnummer: Saksnummer): List<Vedtak> {
        return vedtakJpaRepository.findBySak_Id(saksnummer.id).map { it.toDomain() }
    }


}
