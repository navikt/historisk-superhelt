package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.sak.model.SakEntity
import no.nav.historisk.superhelt.sak.model.SakRepository
import no.nav.historisk.superhelt.sak.model.SakStatus
import no.nav.historisk.superhelt.sak.model.Saksnummer
import no.nav.historisk.superhelt.sak.model.toId
import no.nav.person.Fnr
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SakService(private val sakRepository: SakRepository) {

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.fnr)")
    fun createSak(req: SakCreateRequestDto): SakEntity {
        val sakEntity = SakEntity(
            type = req.type,
            fnr = req.fnr,
            tittel = req.tittel,
            begrunnelse = req.begrunnelse,
            status = SakStatus.UNDER_BEHANDLING,
            saksBehandler = getCurrentNavIdent() ?: "ukjent"
        )
        return sakRepository.save(sakEntity)
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun findSakerForPerson(fnr: Fnr): List<SakDto> {
        return sakRepository.findSakEntitiesByFnr(fnr).map { it.toResponseDto() }
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject?.fnr)")
    fun getSak(saksnummer: Saksnummer): SakDto {
        return getSakOrThrow(saksnummer).toResponseDto()
    }

    private fun getSakOrThrow(saksnummer: Saksnummer): SakEntity {
        return sakRepository.findByIdOrNull(saksnummer.toId())
            ?: throw IkkeFunnetException("Fant ikke sak med saksnummer $saksnummer")
    }

    @PreAuthorize("hasAuthority('WRITE')")
    fun updateSak(saksNummer: Saksnummer, req: SakUpdateRequestDto): SakDto {
        val sak = getSakOrThrow(saksNummer)
        req.tittel?.let { sak.tittel = it }
        req.begrunnelse?.let { sak.begrunnelse = it }
        req.type?.let { sak.type = it }
        return sakRepository.save(sak).toResponseDto()
    }


}