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

private fun SakJpaEntity.toDomain(): Sak {
    return Sak(
        saksnummer = this.id?.let { Saksnummer(it) },
        type = this.type,
        fnr = this.fnr,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        vedtak = this.vedtak,
        saksbehandler = this.saksbehandler,
        opprettetDato = this.opprettet.toLocalDate(),
        soknadsDato = this.soknadsDato
    )
}

private fun Sak.toEntity(): SakJpaEntity {
    return SakJpaEntity(
        id = this.saksnummer?.id,
        type = this.type,
        fnr = this.fnr,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        vedtak = this.vedtak,
        saksbehandler = this.saksbehandler,
        soknadsDato = this.soknadsDato
    )
}
