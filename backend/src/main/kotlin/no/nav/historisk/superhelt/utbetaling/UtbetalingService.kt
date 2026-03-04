package no.nav.historisk.superhelt.utbetaling

import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingKafkaProducer
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UtbetalingService(
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingKafkaProducer: UtbetalingKafkaProducer,
    private val sakEndringsloggService: EndringsloggService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun sendTilUtbetaling(sak: Sak) {
        val utbetaling = utbetalingRepository.findActiveByBehandling(sak)
        if (utbetaling != null) {
            logger.debug(
                "Utbetaling {} finnes allerede for sak {}, sender denne på nytt til utbetaling",
                utbetaling.transaksjonsId,
                sak.saksnummer
            )
            return utbetal(utbetaling, sak)
        }
        if (sak.gjenapnet) {
            return endreUtbetaling(sak)
        }
        opprettNyUtbetaling(sak)?.let { utbetal(it, sak) }
    }

    private fun opprettNyUtbetaling(sak: Sak, tidligereUtbetaling: Utbetaling? = null): Utbetaling? {
        if (sak.utbetalingsType != UtbetalingsType.BRUKER || sak.vedtaksResultat !in listOf(
                VedtaksResultat.INNVILGET,
                VedtaksResultat.DELVIS_INNVILGET
            )
        ) {
            logger.info("Sak ${sak.saksnummer} har utbetalingsType ${sak.utbetalingsType} og resultat ${sak.vedtaksResultat}, ingen utbetaling opprettes")
            return null
        }
        return utbetalingRepository.opprettUtbetaling(sak, tidligereUtbetaling)
    }

    private fun endreUtbetaling(sak: Sak) {
        val tidligereUtbetaling =
            utbetalingRepository.findBySak(sak.saksnummer).sortedByDescending { it.utbetalingTidspunkt }.firstOrNull()
        if (tidligereUtbetaling == null) {
            opprettNyUtbetaling(sak)?.let { utbetal(it, sak) }
        } else {
            logger.info("Sak ${sak.saksnummer} er gjenåpnet, oppretter ny utbetaling basert på tidligere utbetaling ${tidligereUtbetaling.transaksjonsId}")

            when (sak.vedtaksResultat) {
                VedtaksResultat.INNVILGET, VedtaksResultat.DELVIS_INNVILGET -> {
                    endreInnvilgtUtbetaling(sak, tidligereUtbetaling)
                }

                VedtaksResultat.AVSLATT, VedtaksResultat.HENLAGT -> {
                    endreAvslattUtbetaling(sak, tidligereUtbetaling)
                }

                null -> logger.error("Sak ${sak.saksnummer} har null vedtaksresultat, kan ikke opprette ny utbetaling ved gjenåpning")
            }
        }
    }

    private fun endreAvslattUtbetaling(
        sak: Sak,
        tidligereUtbetaling: Utbetaling) {
        logger.info("Sak ${sak.saksnummer} har vedtaksresultat ${sak.vedtaksResultat}, Tidligere utbetaling skal annuleres")
        val annullering = utbetalingRepository.opprettAnnullering(sak, tidligereUtbetaling)
        utbetal(annullering, sak)
    }

    private fun endreInnvilgtUtbetaling(
        sak: Sak,
        tidligereUtbetaling: Utbetaling) {
        if (sak.utbetalingsType != UtbetalingsType.BRUKER) {
            logger.info("Sak ${sak.saksnummer} har utbetalingsType ${sak.utbetalingsType}, Annulerer tidligere utbetaling ${tidligereUtbetaling.transaksjonsId}")
            val annullering = utbetalingRepository.opprettAnnullering(sak, tidligereUtbetaling)
            utbetal(annullering, sak)
        } else {
            opprettNyUtbetaling(sak, tidligereUtbetaling)?.let { utbetal(it, sak) }
        }
    }


    private fun utbetal(utbetaling: Utbetaling, sak: Sak) {
        if (utbetaling.utbetalingStatus !in listOf(UtbetalingStatus.UTKAST, UtbetalingStatus.KLAR_TIL_UTBETALING)) {
            logger.info("Utbetaling ${utbetaling.transaksjonsId} i sak ${sak.saksnummer} er i status ${utbetaling.utbetalingStatus} og vil ikke sendes på nytt til utbetaling")
            return
        }

        utbetalingRepository.setUtbetalingStatus(utbetaling.transaksjonsId, UtbetalingStatus.KLAR_TIL_UTBETALING)
        utbetalingKafkaProducer.sendTilUtbetaling(sak, utbetaling)
    }

    @Transactional
    fun updateUtbetalingsStatus(utbetaling: Utbetaling, newStatus: UtbetalingStatus) {
        val utbetalingsId = utbetaling.transaksjonsId
        val currentStatus = utbetaling.utbetalingStatus
        if (currentStatus.isFinal()) {
            logger.info(
                "Utbetaling {} er i final status {}. Ignoring update til {}",
                utbetalingsId,
                currentStatus,
                newStatus
            )
            return
        }

        if (!currentStatus.shouldBeUpdatedTo(newStatus)) {
            logger.debug(
                "Utbetaling {} er allerede i samme eller nyere status {}, {}. Ignoring update",
                utbetalingsId,
                currentStatus,
                newStatus
            )
            return
        }

        if (newStatus.isFinal()) {
            when (newStatus) {
                UtbetalingStatus.UTBETALT -> sakEndringsloggService.logChange(
                    saksnummer = utbetaling.saksnummer,
                    endringsType = EndringsloggType.UTBETALING_OK,
                    endring = "Kr ${utbetaling.belop} er satt til utbetaling til bruker"
                )

                UtbetalingStatus.FEILET -> sakEndringsloggService.logChange(
                    saksnummer = utbetaling.saksnummer,
                    endringsType = EndringsloggType.UTBETALING_FEILET,
                    endring = "Utbetaling til bruker feilet"
                )

                else -> {}
            }
        }

        utbetalingRepository.setUtbetalingStatus(transaksjonsId = utbetalingsId, status = newStatus)
    }

    @Transactional
    @PreAuthorize("hasAuthority('WRITE')")
    fun retryUtbetaling(sak: Sak) {
        val utbetaling = utbetalingRepository.findActiveByBehandling(sak)
            ?: throw IllegalStateException("Utbetaling er ikke funnet for sak ${sak.saksnummer}")
        if (utbetaling.utbetalingStatus != UtbetalingStatus.FEILET) {
            throw IllegalStateException("Utbetaling i sak ${sak.saksnummer} er i status ${utbetaling.utbetalingStatus} og kan derfor ikke kjøres på nytt")
        }
        utbetalingRepository.setUtbetalingStatus(
            utbetaling.transaksjonsId,
            UtbetalingStatus.KLAR_TIL_UTBETALING
        )
        utbetalingKafkaProducer.sendTilUtbetaling(sak, utbetaling)
    }
}



