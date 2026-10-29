package no.nav.historisk.superhelt.brev

import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.db.BrevJpaEntity
import no.nav.historisk.superhelt.brev.db.BrevJpaRepository
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class BrevRepository(
    private val jpaRepository: BrevJpaRepository,
    private val sakRepository: SakRepository,
    private val entityManager: EntityManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('READ')")
    fun findBySak(saksnummer: Saksnummer): BrevList {
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

    private fun getLockedEntity(uuid: BrevId): BrevJpaEntity {
        entityManager.createNativeQuery("SET LOCAL lock_timeout = '5s'").executeUpdate()
        return getEntityByUUid(uuid).also {
            // Hent fersk status selv om brevet allerede ligger i persistence context.
            entityManager.refresh(it, LockModeType.PESSIMISTIC_WRITE)
        }
    }

    @PreAuthorize("hasAuthority('WRITE')")
    internal fun opprettBrev(brev: Brev): Brev {
        val sakEntity = sakRepository.getSakEntityOrThrow(brev.saksnummer)
        val brevJpaEntity = BrevJpaEntity(
            uuid = brev.uuid,
            createdDate = brev.opprettetTidspunkt,
            sak = sakEntity,
            tittel = brev.tittel,
            innhold = brev.innhold,
            status = brev.status,
            type = brev.type,
            mottakerType = brev.mottakerType,
            journalpostId = brev.journalpostId,
            feil = brev.feil,
        )
        return jpaRepository.save(brevJpaEntity).toDomain()
    }

    @Transactional
    @PreAuthorize("hasAuthority('WRITE')")
    internal fun oppdaterStatus(
        brevId: BrevId,
        status: BrevStatus? = null,
        feil: Boolean? = null,
        journalpostId: EksternJournalpostId? = null): Brev {
        val entity = getLockedEntity(brevId)

        val currentStatus = entity.status
        if (currentStatus == BrevStatus.SENDT) {
            logger.error("Brev er sendt. Ignorerer oppdatering", kv("brevId", brevId), kv("sak", entity.sak.saksnummer))
            return entity.toDomain()
        }
        var oppdateringOk = true

        status?.let {
            if (!currentStatus.tillattNesteStatus(it)) {
                oppdateringOk = false
                logger.info("Statusovergang {} -> {} er ikke tillatt.", currentStatus, it)
            }
        }
        journalpostId?.let {
            if (entity.journalpostId != null && entity.journalpostId != it) {
                oppdateringOk = false
                logger.warn("Forsøker å oppdatere journalpostId på brev  som allerede har journalpostId ${entity.journalpostId}. Ignorerer oppdatering til $it")
            }
        }

        if (oppdateringOk) {
            status?.let { entity.status = it }
            journalpostId?.let { entity.journalpostId = it }
            feil?.let { entity.feil = it }
            return jpaRepository.save(entity).toDomain()
        } else {
            logger.error("Oppdatering av status på  brev  blir ignorert ", kv("brevId", brevId), kv("sak", entity.sak.saksnummer))
            return entity.toDomain()
        }

    }

    @Transactional
    @PreAuthorize("hasAuthority('WRITE')")
    internal fun oppdater(uuid: BrevId, oppdatering: BrevOppdatering): Brev {
        val entity = getLockedEntity(uuid)
        if (entity.status != BrevStatus.UTKAST) {
            logger.warn("Forsøker å oppdatere brev med uuid $uuid  som ikke er i status UTKAST. Status er ${entity.status}.")
            throw IllegalStateException("Kan ikke oppdatere brev som ikke er i status UTKAST")
        }
        oppdatering.tittel?.let { entity.tittel = it }
        oppdatering.innhold?.let { entity.innhold = it }
        return jpaRepository.save(entity).toDomain()
    }

    /** Kun for bruk ved tilbakestilling av feilaktig gjenåpnet sak */
    @Transactional
    @PreAuthorize("hasAuthority('WRITE')")
    internal fun slettBrev(brev: Brev?) {
        if (brev == null) return
        if (getLockedEntity(brev.uuid).status.isLocked()) return
        jpaRepository.deleteByUuid(brev.uuid)
    }

}

internal data class BrevOppdatering(
    val tittel: String? = null,
    val innhold: String? = null,
)

