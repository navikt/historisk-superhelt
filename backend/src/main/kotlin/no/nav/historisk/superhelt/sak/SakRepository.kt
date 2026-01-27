package no.nav.historisk.superhelt.sak

import no.nav.common.types.Aar
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.NavUser
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavUser
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.sak.db.SakJpaRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingUpdateDto
import no.nav.historisk.superhelt.utbetaling.UtbetalingsType
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

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.fnr)")
     fun opprettNySak(req: OpprettSakDto): Sak {
        val properties = req.properties
        val saksbehandler = properties?.saksbehandler ?: getCurrentNavUser()
        val sakEntity = patchEntity(
            dto = properties ?: UpdateSakDto(),
            entity = SakJpaEntity(
                type = req.type,
                fnr = req.fnr,
                status = SakStatus.UNDER_BEHANDLING,
                saksbehandler = saksbehandler
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
        dto.type?.let { entity.type = it }
        dto.status?.let { entity.status = it }
        dto.beskrivelse?.let { entity.beskrivelse = it }
        dto.begrunnelse?.let { entity.begrunnelse = it }
        dto.soknadsDato?.let { entity.soknadsDato = it }
        dto.tildelingsAar?.let { entity.tildelingsAar = it.value }
        dto.vedtaksResultat?.let { entity.vedtaksResultat = it }
        dto.saksbehandler?.let { entity.saksbehandler = it }
        dto.attestant?.let { entity.attestant = if (it == NavUser.NULL_VALUE) null else it }
        dto.utbetalingUpdateDto?.let {
            val belop = it.belop?.value ?: 0
            when (it.utbetalingsType) {
                UtbetalingsType.BRUKER -> {
                    entity.setOrUpdateUtbetaling(belop)
                    entity.forhandstilsagn = null
                }

                UtbetalingsType.FORHANDSTILSAGN -> {
                    entity.setOrUpdateForhandsTilsagn(belop)
                    entity.utbetaling = null
                }

                UtbetalingsType.INGEN -> {
                    entity.utbetaling = null
                    entity.forhandstilsagn = null
                }
            }

        }

        return entity
    }


    private fun getSakEntity(saksnummer: Saksnummer): SakJpaEntity? {
        return jpaRepository.findByIdOrNull(saksnummer.id)
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
    fun getSakEntityOrThrow(saksnummer: Saksnummer): SakJpaEntity {
        return getSakEntity(saksnummer)
            ?: throw IkkeFunnetException("Sak med saksnummer $saksnummer ikke funnet")
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject.fnr)")
    fun getSak(saksnummer: Saksnummer): Sak {
        return getSakEntityOrThrow(saksnummer).toDomain()
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun findSaker(fnr: FolkeregisterIdent): List<Sak> {
        return jpaRepository.findSakEntitiesByFnr(fnr).map { it.toDomain() }
    }
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
    val utbetalingUpdateDto: UtbetalingUpdateDto? = null,
)

