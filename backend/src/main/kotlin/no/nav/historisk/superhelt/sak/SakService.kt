package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.sak.model.SakEntity
import no.nav.historisk.superhelt.sak.model.SakRepository
import no.nav.historisk.superhelt.sak.model.Saksnummer
import no.nav.historisk.superhelt.sak.model.toId
import no.nav.person.Fnr
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SakService(private val sakRepository: SakRepository) {

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.person)")
    fun createSak(req: SakCreateRequestDto): SakEntity {
        val sakEntity = req.toEntity()
        return sakRepository.save(sakEntity)
    }

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun findSakerForPerson(fnr: Fnr): List<SakDto> {
        return sakRepository.findAll().map { it.toResponseDto() }
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject?.person)")
    fun findBySaksnummer(saksnummer: Saksnummer): SakDto? {
        return sakRepository.findByIdOrNull(saksnummer.toId())?.toResponseDto()
    }

    fun updateSak(sak: Any) {}


}