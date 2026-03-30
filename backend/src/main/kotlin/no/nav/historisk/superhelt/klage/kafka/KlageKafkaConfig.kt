package no.nav.historisk.superhelt.klage.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KlageKafkaProperties::class)
class KlageKafkaConfig

/**
 * Konfigurasjon for Kafka-topic der Kabal sender klage-hendingar tilbake.
 * Topic-namnet er avtalt med Kabal-teamet.
 */
@ConfigurationProperties(prefix = "app.klage")
class KlageKafkaProperties(
    /** Topic der Kabal publiserer hendingar om klagebehandling (status, vedtak o.l.) */
    val hendelseTopic: String,
)

