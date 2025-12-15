package no.nav.historisk.superhelt.endringslogg

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.db.EndringsloggJpaEntity
import no.nav.historisk.superhelt.endringslogg.db.EndringsloggJpaRepository
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.sak.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EndringsloggService(
    private val endringsloggJpaRepository: EndringsloggJpaRepository,
    private val sakRepository: SakRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun logChange(saksnummer: Saksnummer, endringsType: EndringsloggType, endring: String, beskrivelse: String? = null) {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        val navBruker = getCurrentNavIdent()
        logger.info("CHANGELOG: Sak $saksnummer endret: $endring av $navBruker")

        endringsloggJpaRepository.save(
            EndringsloggJpaEntity(
                sak = sakEntity,
                endretAv = navBruker,
                type = endringsType,
                endring = endring,
                beskrivelse = beskrivelse
            )
        )
    }

    @Transactional(readOnly = true)
    fun findBySak(saksnummer: Saksnummer): List<EndringsloggLinje> {
        return endringsloggJpaRepository.findBySak_Id(saksnummer.id)
            .map { it.toDomain() }
            .sortedBy { it.endretTidspunkt}
    }
}