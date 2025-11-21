package no.nav.historisk.superhelt.sak

import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository

@Repository
class SakRepository(private val jpaRepository: SakJpaRepository) {

    @PreAuthorize("hasAuthority('WRITE')")
    internal fun save(sak: SakJpaEntity): Sak {
        return jpaRepository.save(sak).toDomain()
    }

    private fun getSakEntity(saksnummer: Saksnummer): SakJpaEntity? {
        return jpaRepository.findByIdOrNull(saksnummer.id)
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
    fun getSakEntityOrThrow(saksnummer: Saksnummer): SakJpaEntity {
        return getSakEntity(saksnummer)
            ?: throw IkkeFunnetException("Sak med saksnummer $saksnummer ikke funnet")
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
    fun getSak(saksnummer: Saksnummer): Sak {
        return getSakEntityOrThrow(saksnummer).toDomain()
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun findSaker(fnr: Fnr): List<Sak> {
        return jpaRepository.findSakEntitiesByFnr(fnr).map { it.toDomain() }
    }
}
