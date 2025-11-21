package no.nav.historisk.superhelt.utbetaling.db

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UtbetalingMetrics(private val meterRegistry: MeterRegistry) {


    @Bean
    fun utbetalingStatusGaugeMetric(utbetalingRepository: UtbetalingJpaRepository): String {
        UtbetalingStatus.entries.forEach { status ->
            Gauge.builder("db.utbetalinger.count") {
                utbetalingRepository.countByUtbetalingStatus(status).toDouble()
            }
                .description("Antall utbetalinger med status $status")
                .tag("status", status.name)
                .register(meterRegistry)
        }
        return "Utbetaling metrics registered"
    }

}