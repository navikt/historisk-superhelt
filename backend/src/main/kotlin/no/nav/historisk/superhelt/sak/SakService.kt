package no.nav.historisk.superhelt.sak

import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.BrevRepository
import no.nav.historisk.superhelt.infrastruktur.authentication.NavUser
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.infrastruktur.validation.ValidationFieldError
import no.nav.historisk.superhelt.vedtak.Vedtak
import no.nav.historisk.superhelt.vedtak.VedtakRepository
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

            SakStatus.FEILREGISTRERT -> UpdateSakDto(
                status = nyStatus,
                saksbehandler = getAuthenticatedUser().navUser,
                attestant = NavUser.NULL_VALUE
            )
        }

        sakRepository.updateSak(saksnummer, updateDto)
        logger.debug("Sak {} endret status til {}", saksnummer, nyStatus)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun gjenapneSak(sak: Sak) {
        endreStatus(sak, SakStatus.UNDER_BEHANDLING)
        sakRepository.incrementBehandlingsNummer(sak.saksnummer)
        logger.info("Sak {} er gjenåpnet", sak.saksnummer)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun tilbakestillGjenapning(sak: Sak,sisteVedtak: Vedtak) {
        sakRepository.tilbakestillFraSistVedtak(sak.saksnummer, sisteVedtak)
        logger.info("Sak {} er tilbakestilt etter feilaktig gjenåpning", sak.saksnummer)
    }

}
