package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.sak.model.SakEntity
import no.nav.historisk.superhelt.sak.model.SakRepository
import no.nav.historisk.superhelt.sak.model.Saksnummer
import no.nav.historisk.superhelt.sak.model.toId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SakService(private val sakRepository: SakRepository) {

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.person)")
    fun createSak(req: SakCreateRequestDto): SakEntity {
        val sakEntity = SakEntity(
            type = req.type,
            person = req.person,
            tittel = req.tittel,
            begrunnelse = req.begrunnelse
        )
        return sakRepository.save(sakEntity)
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostFilter("@tilgangsmaskin.harTilgang(filterObject.person)")
    fun findAll(): List<SakDto> {
        return sakRepository.findAll().map { it.toResponseDto() }
    }

    @PreAuthorize("hasAuthority('READ')")
    @PostAuthorize("@tilgangsmaskin.harTilgang(returnObject?.person)")
    fun findBySaksnummer(saksnummer: Saksnummer): SakDto? {
        return sakRepository.findByIdOrNull(saksnummer.toId())?.toResponseDto()
    }

//    private fun generateSaknummer(type : StonadsType ): Saksnummer{
//        val prefix = when(type) {
//            StonadsType.PARYKK -> "PAR"
//            StonadsType.ORTOSE -> "ORT"
//            StonadsType.PROTESE -> "PRO"
//            StonadsType.FOTTOY -> "FOT"
//            StonadsType.REISEUTGIFTER -> "RUT"
//        }
//        val nextNumber = sakRepository.hentNesteSaksnummer()
//        return "$prefix${nextNumber.toString().padStart(6, '0')}"
//    }


}