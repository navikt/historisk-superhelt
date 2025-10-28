package no.nav.historisk.superhelt.sak.db

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.person.Fnr
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class SakRepositoryImpl(private val jpaRepository: SakJpaRepository) : SakRepository {
    override fun save(sak: Sak): Sak {
        return jpaRepository.save(sak.toEntity()).toDomain()
    }

    override fun getSak(saksnummer: Saksnummer): Sak? {
        return jpaRepository.findByIdOrNull(saksnummer.id)?.toDomain()
    }

    override fun findSaker(fnr: Fnr): List<Sak> {
        return jpaRepository.findSakEntitiesByFnr(fnr).map { it.toDomain() }
    }
}


