package no.nav.historisk.mock.statistikk

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StatistikkConfigProperties::class)
class StatistikkConfig(private val properties: StatistikkConfigProperties) {

    @Bean
    fun behandlingStatistikkTopic(): NewTopic {
        return NewTopic(properties.saksBehandlingStatistikkTopic, 1, 1)
    }
}

@ConfigurationProperties(prefix = "app.statistikk")
class StatistikkConfigProperties(val saksBehandlingStatistikkTopic: String) {}
