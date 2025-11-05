package no.nav.historisk.superhelt.utbetaling

import no.nav.helved.Periode
import no.nav.helved.Periodetype
import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.Sak
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Service
class UtbetalingService(
    private val kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>,
    private val properties: UtbetalingConfigProperties
) {


    fun sendTilUtbetaling(sak: Sak) {
        val melding = UtbetalingMelding(
            id = UUID.randomUUID().toString(),
            sakId = sak.saksnummer.value,
            behandlingId = "behandlingId",
            personident = sak.fnr.value,
            stønad = "stønad",
            vedtakstidspunkt = Instant.now(),
            periodetype = Periodetype.DAG,
            perioder = listOf(
                Periode(
                    fom = LocalDate.now(),
                    tom = LocalDate.now(),
                    beløp = sak.utbetaling?.belop?.toInt() ?: 0
                )

            ),
            saksbehandler = sak.saksbehandler,
            beslutter = sak.saksbehandler
        )

        val result = kafkaTemplate.send(
            properties.utbetalingTopic, melding
        )
            .get()
        println("Melding sendt til topic superhelt-utbetalinger ${result}")
    }

}