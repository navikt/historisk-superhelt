package no.nav.historisk.superhelt.endringslogg

import no.nav.common.types.NavIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.db.EndringsloggJpaEntity
import no.nav.historisk.superhelt.endringslogg.db.EndringsloggJpaRepository
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.sak.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class EndringsloggService(
    private val endringsloggJpaRepository: EndringsloggJpaRepository,
    private val sakRepository: SakRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun logChange(saksnummer: Saksnummer,
                  endringsType: EndringsloggType,
                  navBruker: NavIdent = getAuthenticatedUser().navIdent,
                  endring: String,
                  beskrivelse: String? = null,
                  tidspunkt: Instant = Instant.now()) {
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        logger.info("CHANGELOG: Sak $saksnummer endret: $endring av $navBruker")

        endringsloggJpaRepository.save(
            EndringsloggJpaEntity(
                sak = sakEntity,
                endretAv = navBruker,
                type = endringsType,
                endring = endring,
                beskrivelse = beskrivelse,
                tidspunkt = tidspunkt
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