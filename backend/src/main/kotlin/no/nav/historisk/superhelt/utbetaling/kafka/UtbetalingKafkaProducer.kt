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
        val melding =
            UtbetalingMelding(
                id = id,
                sakId = sak.saksnummer.value,
                behandlingId = id,
                personident = sak.fnr.value,
                stønad = "KLASSEKODE",
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

        logger.debug("Sender til utbetaling {}:{}", utbetalingTopic, id)
        val result = kafkaTemplate.send(utbetalingTopic, id, melding).get()
        //TODO håndter feilsituasjoner
        utbetalingRepository.setUtbetalingStatusSendt(utbetaling.uuid)

    }
}
