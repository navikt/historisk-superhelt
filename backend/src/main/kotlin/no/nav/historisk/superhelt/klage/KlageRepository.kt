package no.nav.historisk.superhelt.klage

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.klage.db.KlageJpaEntity
import no.nav.historisk.superhelt.klage.db.KlageJpaRepository
import no.nav.historisk.superhelt.sak.SakRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate

@Repository
class KlageRepository(
    private val klageJpaRepository: KlageJpaRepository,
    private val sakRepository: SakRepository,
) {

    @PreAuthorize("hasAuthority('WRITE')")
    fun lagreKlage(
        saksnummer: Saksnummer,
        hjemmelId: String,
        datoKlageMottatt: LocalDate,
        kommentar: String?,
        kabalBehandlingId: String?,
        status: KlageStatus,
    ): Klage {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        val innloggetBruker = getAuthenticatedUser()

        val entity = KlageJpaEntity(
            sak = sakEntity,
            hjemmelId = hjemmelId,
            datoKlageMottatt = datoKlageMottatt,
            kommentar = kommentar,
            kabalBehandlingId = kabalBehandlingId,
            opprettetTidspunkt = Instant.now(),
            opprettetAv = innloggetBruker.navIdent.value,
            status = status,
        )

        return klageJpaRepository.save(entity).toDomain()
    }

    @PreAuthorize("hasAuthority('READ')")
    fun hentKlagerForSak(saksnummer: Saksnummer): List<Klage> {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        return klageJpaRepository.findBySakId(sakEntity.id!!)
            .map { it.toDomain() }
    }

    @PreAuthorize("hasAuthority('READ')")
    fun hentKlageByKabalBehandlingId(kabalBehandlingId: String): Klage? {
        return klageJpaRepository.findByKabalBehandlingId(kabalBehandlingId)?.toDomain()
    }
}
