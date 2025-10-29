package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import no.nav.historisk.superhelt.sak.db.toDomain
import no.nav.historisk.superhelt.sak.db.toEntity
import no.nav.person.Fnr
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository

@Repository
class SakRepository(private val jpaRepository: SakJpaRepository)  {
    @PreAuthorize("hasAuthority('WRITE')")
     fun save(sak: Sak): Sak {
        return jpaRepository.save(sak.toEntity()).toDomain()
    }
    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
     fun getSak(saksnummer: Saksnummer): Sak? {
        return jpaRepository.findByIdOrNull(saksnummer.id)?.toDomain()
    }

    fun getSakOrThrow(saksnummer: Saksnummer): Sak {
        return getSak(saksnummer)
            ?: throw IkkeFunnetException("Sak med saksnummer $saksnummer ikke funnet")
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
     fun findSaker(fnr: Fnr): List<Sak> {
        return jpaRepository.findSakEntitiesByFnr(fnr).map { it.toDomain() }
    }
}