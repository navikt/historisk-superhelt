package no.nav.historisk.superhelt.utbetaling

import no.nav.helved.Periode
import no.nav.helved.Periodetype
import no.nav.helved.UtbetalingMelding
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.Utbetaling
import no.nav.historisk.superhelt.sak.db.UtbetalingJpaEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
class UtbetalingService(private val kafkaTemplate: KafkaTemplate<String, UtbetalingMelding>) {

    fun sendTilUtbetaling(sak: Sak) {
        val melding = UtbetalingMelding(
            id = UUID.randomUUID().toString(),
            sakId = sak.saksnummer.value,
            behandlingId = "behandlingId",
            personident = "123456789012",
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
        val key= UUID.randomUUID().toString()
        val result = kafkaTemplate.send(
            "superhelt-utbetalinger", key,  melding)
            .get()
        println("Melding sendt til topic superhelt-utbetalinger ${result}")
    }

}