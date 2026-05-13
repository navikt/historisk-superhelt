package no.nav.historisk.superhelt.sak

import no.nav.common.types.Aar
import no.nav.common.types.Belop
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.helved.KlasseKode
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.ansatt.Enheter
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
import no.nav.historisk.superhelt.vedtak.Vedtak
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Repository
class SakRepository(private val jpaRepository: SakJpaRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.fnr) and @temaAuth.harTilgang(#req.type.tema)")
    fun opprettNySak(req: OpprettSakDto): Sak {
        val properties = req.properties
        val saksbehandler = properties?.saksbehandler ?: getAuthenticatedUser().navUser
        val sakEntity = patchEntity(
            dto = properties ?: UpdateSakDto(),
            entity = SakJpaEntity(
                type = req.type,
                fnr = req.fnr,
                status = SakStatus.UNDER_BEHANDLING,
                saksbehandler = saksbehandler,
                enhet = properties?.enhet?: Enheter.guessEnhet(req.type)
            )
        )
        val saved = jpaRepository.save(sakEntity)
        logger.info("Opprettet ny sak med saksnummer {}", saved.saksnummer)
        return saved.toDomain()
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun updateSak(saksnummer: Saksnummer, req: UpdateSakDto): Sak {
        val sakEntity = patchEntity(dto = req, entity = getSakEntityOrThrow(saksnummer))
        logger.debug("Oppdaterer sak med saksnummer {}", saksnummer)
        return jpaRepository.save(sakEntity).toDomain()
    }

    private fun patchEntity(dto: UpdateSakDto, entity: SakJpaEntity): SakJpaEntity {
        dto.type?.let {
            if (it != entity.type) {
                entity.type = it
                // nullstiller for å unngå å få ugyldige klassekoder
                entity.klassekode = null
            }
        }
        dto.status?.let { entity.status = it }
        dto.beskrivelse?.let { entity.beskrivelse = it }
        dto.begrunnelse?.let { entity.begrunnelse = it }
        dto.soknadsDato?.let { entity.soknadsDato = it }
        dto.tildelingsAar?.let { entity.tildelingsAar = it.value }
        dto.vedtaksResultat?.let { entity.vedtaksResultat = it }
        dto.saksbehandler?.let { entity.saksbehandler = it }
        dto.enhet?.let { entity.enhet = it }
        dto.attestant?.let { entity.attestant = if (it == NavUser.NULL_VALUE) null else it }

        dto.utbetalingsType?.let { entity.utbetalingsType = it }
        val utbetalingsType = dto.utbetalingsType ?: entity.utbetalingsType
        val isUtbetalTilBruker = utbetalingsType == UtbetalingsType.BRUKER
        if (isUtbetalTilBruker) {
            dto.belop?.let { entity.belop = it.value }
            dto.klasseKode?.let { entity.klassekode = it }
        } else {
            entity.belop = null
            entity.klassekode = null
        }

        return entity
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun incrementBehandlingsNummer(saksnummer: Saksnummer): Sak {
        val sakEntity = getSakEntityOrThrow(saksnummer)
        sakEntity.behandlingsTeller += 1
        return jpaRepository.save(sakEntity).toDomain()
    }

    /** Kun for bruk ved tilbakestilling av feilaktig gjenåpnet sak */
    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    internal fun tilbakestillFraSistVedtak(saksnummer: Saksnummer, vedtak: Vedtak): Sak {
        val sakEntity = getSakEntityOrThrow(saksnummer)
        sakEntity.status = SakStatus.FERDIG
        sakEntity.behandlingsTeller -= 1
        sakEntity.saksbehandler = vedtak.saksbehandler
        sakEntity.attestant = vedtak.attestant
        sakEntity.beskrivelse = vedtak.beskrivelse
        sakEntity.begrunnelse = vedtak.begrunnelse
        sakEntity.soknadsDato = vedtak.soknadsDato
        sakEntity.tildelingsAar = vedtak.tildelingsAar?.value
        sakEntity.vedtaksResultat = vedtak.resultat
        sakEntity.utbetalingsType = vedtak.utbetalingsType
        sakEntity.belop = vedtak.belop?.value
        sakEntity.klassekode = vedtak.klasseKode
        sakEntity.enhet = vedtak.enhet
        return jpaRepository.save(sakEntity).toDomain()
    }


    private fun getSakEntity(saksnummer: Saksnummer): SakJpaEntity? {
        return jpaRepository.findByIdOrNull(saksnummer.id)
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr) and @temaAuth.harTilgang(returnObject.type.tema)")
    fun getSakEntityOrThrow(saksnummer: Saksnummer): SakJpaEntity {
        return getSakEntity(saksnummer)
            ?: throw IkkeFunnetException("Sak med saksnummer $saksnummer ikke funnet")
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr) and @temaAuth.harTilgang(returnObject.type.tema)")
    fun getSak(saksnummer: Saksnummer): Sak {
        return getSakEntityOrThrow(saksnummer).toDomain()
    }

    /** Henter saker for en person og filterer på tema som personen har tilgang til  */
    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun finnSaker(fnr: FolkeregisterIdent): List<Sak> {
        val authenticatedUser= getAuthenticatedUser()
        return jpaRepository.findSakEntitiesByFnr(fnr)
            .filter { authenticatedUser.hasTemaAccess(it.type.tema) }
            .map { it.toDomain() }
    }

    @PreAuthorize("hasAuthority('READ')")
    internal fun finnAapneSaker(): List<Sak> =
        jpaRepository.findByStatusNotIn(listOf(SakStatus.FERDIG, SakStatus.FEILREGISTRERT))
            .map { it.toDomain() }
}

data class OpprettSakDto(
    val type: StonadsType,
    val fnr: FolkeregisterIdent,
    val properties: UpdateSakDto? = null,
)

data class UpdateSakDto(
    val type: StonadsType? = null,
    val status: SakStatus? = null,
    val beskrivelse: String? = null,
    val begrunnelse: String? = null,
    val soknadsDato: LocalDate? = null,
    val tildelingsAar: Aar? = null,
    val vedtaksResultat: VedtaksResultat? = null,
    val saksbehandler: NavUser? = null,
    val attestant: NavUser? = null,
    val utbetalingsType: UtbetalingsType? = null,
    val belop: Belop? = null,
    val klasseKode: KlasseKode? = null,
    val enhet: Enhetsnummer? = null
)

