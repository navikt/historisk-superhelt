package no.nav.historisk.superhelt.statistikk.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StatistikkConfigProperties::class)
class StatistikkConfig {}

@ConfigurationProperties(prefix = "app.statistikk")
class StatistikkConfigProperties(val saksBehandlingStatistikkTopic: String) {}
