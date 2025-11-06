package no.nav.historisk.superhelt.utbetaling

import java.time.Instant
import java.time.LocalDate
import java.util.*
import no.nav.helved.Periode
import no.nav.helved.Periodetype
import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class UtbetalingService(
  private val kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>,
  properties: UtbetalingConfigProperties,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  private val utbetalingTopic = properties.utbetalingTopic

  fun sendTilUtbetaling(sak: Sak) {
    val melding =
      UtbetalingMelding(
        id = UUID.randomUUID().toString(),
        sakId = sak.saksnummer.value,
        behandlingId = "behandlingId",
        personident = sak.fnr.value,
        stønad = "stønad",
        vedtakstidspunkt = Instant.now(),
        periodetype = Periodetype.DAG,
        perioder =
          listOf(
            Periode(
              fom = LocalDate.now(),
              tom = LocalDate.now(),
              beløp = sak.utbetaling?.belop?.toInt() ?: 0,
            )
          ),
        saksbehandler = sak.saksbehandler,
        beslutter = sak.saksbehandler,
      )

    logger.debug("Sender til utbetaling {}", utbetalingTopic)
    val utbetalingTopic = utbetalingTopic
    val result = kafkaTemplate.send(utbetalingTopic, melding).get()
    logger.info("Melding sendt til topic {}, {}", utbetalingTopic, result)
  }
}
