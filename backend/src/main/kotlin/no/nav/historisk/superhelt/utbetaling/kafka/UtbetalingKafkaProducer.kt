package no.nav.historisk.superhelt.utbetaling.kafka

import no.nav.helved.Periode
import no.nav.helved.Periodetype
import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

@Service
class UtbetalingKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>,
    properties: UtbetalingConfigProperties,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val utbetalingTopic = properties.utbetalingTopic

    fun sendTilUtbetaling(sak: Sak, utbetaling: Utbetaling) {
        val melding =
            UtbetalingMelding(
                id = utbetaling.uuid.toString(),
                sakId = sak.saksnummer.value,
                behandlingId = utbetaling.uuid.toString(),
                personident = sak.fnr.value,
                stønad = "stønad",
                vedtakstidspunkt = Instant.now(),
                periodetype = Periodetype.DAG,
                perioder =
                    listOf(
                        Periode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now(),
                            beløp = utbetaling.belop
                        )
                    ),
                saksbehandler = sak.saksbehandler,
                beslutter = sak.saksbehandler,
            )

        logger.debug("Sender til utbetaling {}", utbetalingTopic)
        val result = kafkaTemplate.send(utbetalingTopic, melding).get()
        //TODO håndter feilsituasjoner

    }
}
