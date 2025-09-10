package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.sak.model.SakEntity
import no.nav.historisk.superhelt.sak.model.SakRepository
import no.nav.historisk.superhelt.sak.model.Saksnummer
import no.nav.historisk.superhelt.sak.model.StonadsType
import org.springframework.stereotype.Service

@Service
class SakService(private val sakRepository: SakRepository) {

    fun createSak(req: SakCreateRequestDto): SakEntity {
        val sakEntity = SakEntity(
            saksnummer = generateSaknummer(req.type),
            type = req.type,
            person = req.person,
            tittel = req.tittel,
            begrunnelse = req.begrunnelse
        )
        return sakRepository.save(sakEntity)
    }

    private fun generateSaknummer(type : StonadsType ): Saksnummer{
        val prefix = when(type) {
            StonadsType.PARYKK -> "PAR"
            StonadsType.ORTOSE -> "ORT"
            StonadsType.PROTESE -> "PRO"
            StonadsType.FOTTOY -> "FOT"
            StonadsType.REISEUTGIFTER -> "RUT"
        }
        val nextNumber = sakRepository.hentNesteSaksnummer()
        return "$prefix${nextNumber.toString().padStart(6, '0')}"
    }


}