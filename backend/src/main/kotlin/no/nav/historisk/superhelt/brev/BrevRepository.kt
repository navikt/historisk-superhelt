package no.nav.historisk.superhelt.brev

import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.db.BrevJpaEntity
import no.nav.historisk.superhelt.brev.db.BrevJpaRepository
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.SakRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class BrevRepository(
    private val jpaRepository: BrevJpaRepository,
    private val sakRepository: SakRepository,
) {

    @PreAuthorize("hasAuthority('READ')")
    fun findBySak(saksnummer: Saksnummer): BrevUtkastList {
        return jpaRepository.findAllBySakId(saksnummer.id).map { it.toDomain() }
    }

    private fun getEntityByUUid(uuid: BrevId): BrevJpaEntity {
        return jpaRepository.findByUuid(uuid)
            ?: throw IkkeFunnetException("Brev med uuid $uuid ikke funnet")
    }

    @PreAuthorize("hasAuthority('READ')")
    fun getByUUid(uuid: BrevId): Brev {
        return getEntityByUUid(uuid).toDomain()
    }

    @PreAuthorize("hasAuthority('WRITE')")
    internal fun opprettBrev(brev: Brev): Brev {
        val sakEntity = sakRepository.getSakEntityOrThrow(brev.saksnummer)
        val brevJpaEntity = BrevJpaEntity(
            uuid = brev.uuid,
            sak = sakEntity,
            tittel = brev.tittel,
            innhold = brev.innhold,
            status = brev.status,
            type = brev.type,
            mottakerType = brev.mottakerType,
            journalpostId = brev.journalpostId,
        )
        return jpaRepository.save(brevJpaEntity).toDomain()
    }

    @Transactional
    @PreAuthorize("hasAuthority('WRITE')")
    internal fun oppdater(uuid: BrevId, oppdatering: BrevOppdatering): Brev {
        val entity = getEntityByUUid(uuid)
        if (entity.status == BrevStatus.SENDT) {
            throw IllegalStateException("Kan ikke oppdatere brev som er sendt")
        }
        oppdatering.tittel?.let { entity.tittel = it }
        oppdatering.innhold?.let { entity.innhold = it }
        oppdatering.status?.let { entity.status = it }
        oppdatering.journalpostId?.let { entity.journalpostId = it }
        return jpaRepository.save(entity).toDomain()
    }


}

internal data class BrevOppdatering(
    val tittel: String? = null,
    val innhold: String? = null,
    val status: BrevStatus? = null,
    val journalpostId: EksternJournalpostId? = null
)

typealias BrevUtkastList = List<Brev>

fun BrevUtkastList.findBrev(type: BrevType, mottaker: BrevMottaker): Brev? =
    this.find { it.type == type && it.mottakerType == mottaker  }