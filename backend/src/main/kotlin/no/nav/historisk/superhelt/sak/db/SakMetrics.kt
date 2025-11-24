package no.nav.historisk.superhelt.sak.db

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SakMetrics(private val meterRegistry: MeterRegistry) {

    @Bean
    fun sakGaugeMetric(sakRepository: SakJpaRepository): String {

        SakStatus.entries.forEach { status ->
            StonadsType.entries.forEach { type ->
                Gauge.builder("db.saker.count") {
                    sakRepository.countByTypeAndStatus(type, status).toDouble()
                }
                    .description("Antall saker av type $type med status $status")
                    .tag("type", type.name)
                    .tag("status", status.name)
                    .register(meterRegistry)
            }
        }
        return "Sak metrics registered"
    }

}