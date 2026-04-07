package no.nav.historisk.superhelt.klage

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.klage.db.KlageJpaEntity
import no.nav.historisk.superhelt.klage.db.KlageJpaRepository
import no.nav.historisk.superhelt.sak.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Repository
class KlageRepository(
    private val klageJpaRepository: KlageJpaRepository,
    private val sakRepository: SakRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('READ')")
    fun findBySak(saksnummer: Saksnummer): List<Klage> =
        klageJpaRepository.findBySakId(saksnummer.id).map { it.toDomain() }

    @PreAuthorize("hasAuthority('READ')")
    fun findById(id: UUID): Klage? =
        klageJpaRepository.findByIdOrNull(id)?.toDomain()

    @PreAuthorize("hasAuthority('WRITE')")
    fun lagreKlage(
        saksnummer: Saksnummer,
        hjemmelId: String,
        datoKlageMottatt: LocalDate,
        kommentar: String?,
        forrigeBehandlendeEnhet: String,
        sendtTidspunkt: Instant = Instant.now(),
    ): Klage {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        val entity = KlageJpaEntity(
            id = UUID.randomUUID(),
            sak = sakEntity,
            hjemmelId = hjemmelId,
            datoKlageMottatt = datoKlageMottatt,
            kommentar = kommentar,
            forrigeBehandlendeEnhet = forrigeBehandlendeEnhet,
            sendtTidspunkt = sendtTidspunkt,
            status = KlageStatus.SENDT,
        )
        val klage = klageJpaRepository.save(entity).toDomain()
        logger.info("Lagra klage {} for sak {}", klage.id, saksnummer)
        return klage
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun oppdaterStatus(id: UUID, nyStatus: KlageStatus): Klage? {
        val entity = klageJpaRepository.findByIdOrNull(id) ?: run {
            logger.warn("Fant ikkje klage med id {} ved statusoppdatering", id)
            return null
        }
        entity.status = nyStatus
        return klageJpaRepository.save(entity).toDomain().also {
            logger.info("Oppdaterte klage {} til status {}", id, nyStatus)
        }
    }
}







