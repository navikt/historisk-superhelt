package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.KlasseKode
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
        val id = utbetaling.uuid.toString()
        // TODO vedtakstidspunkt fra sak/utbetaling når vi har det
        val vedtaksTidspunkt = utbetaling.utbetalingTidspunkt ?: Instant.now()
        val melding =
            UtbetalingMelding(
                id = id,
                sakId = sak.saksnummer.value,
                behandlingId = id.take(30), // max 30 tegn TODO Innføre ett
                personident = sak.fnr.value,
                stønad = KlasseKode.TILSKUDD_SMÅHJELPEMIDLER,

                vedtakstidspunkt = vedtaksTidspunkt,
                periodetype = Periodetype.EN_GANG,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.ofInstant(vedtaksTidspunkt, ZoneOffset.systemDefault()),
                            tom = LocalDate.ofInstant(vedtaksTidspunkt, ZoneOffset.systemDefault()),
                            beløp = utbetaling.belop.value
                        )
                    ),
                saksbehandler = sak.saksbehandler.navIdent.value,
                beslutter = sak.attestant?.navIdent?.value ?: sak.saksbehandler.navIdent.value,
            )

        logger.debug("Sender til utbetaling {}:{}", utbetalingTopic, id)
        val result = kafkaTemplate.send(utbetalingTopic, id, melding).get()
        //TODO håndter feilsituasjoner
        utbetalingRepository.setUtbetalingStatusSendt(utbetaling.uuid)

    }
}
