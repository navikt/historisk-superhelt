package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import no.nav.person.Fnr
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository

@Repository
class SakRepository(private val jpaRepository: SakJpaRepository) {

    @PreAuthorize("hasAuthority('WRITE')")
    fun save(sak: SakJpaEntity): Sak {
        return jpaRepository.save(sak).toDomain()
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
    fun getSakEntity(saksnummer: Saksnummer): SakJpaEntity? {
        return jpaRepository.findByIdOrNull(saksnummer.id)
    }


    fun getSakEntityOrThrow(saksnummer: Saksnummer): SakJpaEntity {
        return getSakEntity(saksnummer)
            ?: throw IkkeFunnetException("Sak med saksnummer $saksnummer ikke funnet")
    }

    fun getSakOrThrow(saksnummer: Saksnummer): Sak {
        return getSakEntityOrThrow(saksnummer).toDomain()
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun findSaker(fnr: Fnr): List<Sak> {
        return jpaRepository.findSakEntitiesByFnr(fnr).map { it.toDomain() }
    }
}

