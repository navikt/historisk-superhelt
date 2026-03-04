package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.Periode
import no.nav.helved.Periodetype
import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class UtbetalingKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>,
    private val utbetalingRepository: UtbetalingRepository,
    properties: UtbetalingConfigProperties,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val utbetalingTopic = properties.utbetalingTopic

    @Transactional
    fun sendTilUtbetaling(sak: Sak, utbetaling: Utbetaling) {
        val id = utbetaling.utbetalingsUuid
        val key = utbetaling.transaksjonsId
        val vedtaksTidspunkt = utbetaling.utbetalingTidspunkt ?: Instant.now()

        val perioder: List<Periode> = if (utbetaling.annulleres) {
            emptyList()
        } else {
            listOf(
                Periode(
                    fom = LocalDate.ofInstant(vedtaksTidspunkt, ZoneOffset.systemDefault()),
                    tom = LocalDate.ofInstant(vedtaksTidspunkt, ZoneOffset.systemDefault()),
                    beløp = utbetaling.belop.value
                )
            )
        }
        
        val utbetalingMelding = UtbetalingMelding(
            id = id,
            sakId = sak.saksnummer.value,
            behandlingId = sak.behandlingsnummer.toString(),
            personident = sak.fnr.value,
            stønad = sak.type.klassekode,

            vedtakstidspunkt = vedtaksTidspunkt,
            periodetype = Periodetype.EN_GANG,
            perioder = perioder,
            saksbehandler = sak.saksbehandler.navIdent.value,
            beslutter = sak.attestant?.navIdent?.value ?: sak.saksbehandler.navIdent.value,
        )

       

        if (perioder.isEmpty()) {
            logger.info("Annulerer utbetaling {} for sak {}, sender melding med tomme perioder", utbetaling.transaksjonsId, sak.saksnummer)
        }

        logger.debug("Sender melding til utbetaling {}:{}", utbetalingTopic, key)
        kafkaTemplate.send(utbetalingTopic, key.toString(), utbetalingMelding).get()
        utbetalingRepository.setUtbetalingStatusSendt(utbetaling.transaksjonsId)
    }
}
