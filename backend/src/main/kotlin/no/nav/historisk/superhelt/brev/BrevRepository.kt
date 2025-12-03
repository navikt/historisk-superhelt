package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.brev.db.BrevJpaRepository
import no.nav.historisk.superhelt.brev.db.BrevutkastJpaEntity
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.Saksnummer
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository

@Repository
class BrevRepository(
    private val jpaRepository: BrevJpaRepository,
    private val sakRepository: SakRepository,
) {

    @PreAuthorize("hasAuthority('READ')")
    fun findBySak(saksnummer: Saksnummer): BrevUtkastList {
        return jpaRepository.findAllBySakId(saksnummer.id).map { it.toDomain() }
    }

    private fun getEntityByUUid(uuid: BrevId): BrevutkastJpaEntity {
        return jpaRepository.findByUuid(uuid)
            ?: throw IkkeFunnetException("Brev med uuid $uuid ikke funnet")
    }

    @PreAuthorize("hasAuthority('READ')")
    fun getByUUid(uuid: BrevId): BrevUtkast {
        return getEntityByUUid(uuid).toDomain()
    }

    @PreAuthorize("hasAuthority('WRITE')")
    internal fun opprettBrev(saksnummer: Saksnummer, brevUtkast: BrevUtkast): BrevUtkast {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        val brevJpaEntity = BrevutkastJpaEntity(
            uuid = brevUtkast.uuid,
            sak = sakEntity,
            tittel = brevUtkast.tittel,
            innhold = brevUtkast.innhold,
            status = BrevStatus.NY,
            type = brevUtkast.type,
            mottakerType = brevUtkast.mottakerType,
        )
        return jpaRepository.save(brevJpaEntity).toDomain()
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun lagre(oppdatertBrev: BrevUtkast): BrevUtkast {
        val entity = getEntityByUUid(oppdatertBrev.uuid)
        entity.tittel = oppdatertBrev.tittel
        entity.innhold = oppdatertBrev.innhold
        entity.status = oppdatertBrev.status
        return jpaRepository.save(entity).toDomain()
    }


}

typealias BrevUtkastList = List<BrevUtkast>

fun BrevUtkastList.findBrev(type: BrevType, mottaker: BrevMottaker): BrevUtkast? =
    this.find { it.type == type && it.mottakerType == mottaker }