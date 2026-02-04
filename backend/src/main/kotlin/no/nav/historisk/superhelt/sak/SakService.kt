package no.nav.historisk.superhelt.sak

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.sak.rest.UtbetalingRequestDto
import no.nav.historisk.superhelt.utbetaling.UtbetalingUpdateDto
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SakService(
    private val sakRepository: SakRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun updateUtbetaling(saksnummer: Saksnummer, req: UtbetalingRequestDto): Sak {
        val sak = sakRepository.getSak(saksnummer)

        val updateDto = UpdateSakDto(
            utbetalingUpdateDto = UtbetalingUpdateDto(
                belop = req.belop,
                utbetalingsType = req.utbetalingsType
            )
        )

        logger.info("Oppdaterer sak med saksnummer {}", saksnummer)
        return sakRepository.updateSak(saksnummer, updateDto)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun endreStatus(sak: Sak, nyStatus: SakStatus) {
        val saksnummer = sak.saksnummer
        if (nyStatus == sak.status) {
            logger.debug("Sak {} status er allerede {}, ingen endring gjort.", saksnummer, nyStatus)
            return
        }

        val updateDto = when (nyStatus) {
            SakStatus.UNDER_BEHANDLING -> UpdateSakDto(
                status = nyStatus,
                saksbehandler = getAuthenticatedUser().navUser,
                attestant = NavUser.NULL_VALUE
            )

            SakStatus.TIL_ATTESTERING -> UpdateSakDto(
                status = nyStatus,
                saksbehandler = getAuthenticatedUser().navUser,
                attestant = NavUser.NULL_VALUE
            )

            SakStatus.FERDIG_ATTESTERT -> UpdateSakDto(
                status = nyStatus,
                attestant = getAuthenticatedUser().navUser
            )

            SakStatus.FERDIG -> UpdateSakDto(
                status = nyStatus,
                attestant = getAuthenticatedUser().navUser
            )


        }

        sakRepository.updateSak(saksnummer, updateDto)
        logger.info("Sak {} endret status til {}", saksnummer, nyStatus)
    }

}
