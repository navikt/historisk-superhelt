package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.klage.config.KabalProperties
import no.nav.historisk.superhelt.statistikk.kafka.StatistikkConfigProperties
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingConfigProperties
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID

@TestConfiguration(proxyBeanMethods = false)
class KafkaTestcontainersConfiguration(
    private val utbetalingProperties: UtbetalingConfigProperties,
    private val statistikkProperties: StatistikkConfigProperties,
    private val kabalProperties: KabalProperties) {

    @Bean
    @ServiceConnection
    @Profile("!sandkasse")
    fun kafkaContainer(): ConfluentKafkaContainer {
        return ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:8.2.0"))
            .withReuse(System.getenv("CI") == null)
    }

    @Bean
    @Profile("sandkasse")
    fun sandkasseKafkaProperties(): DynamicPropertyRegistrar {
        val transactionIdPrefix = "test-${UUID.randomUUID()}-"
        return DynamicPropertyRegistrar { registry ->
            registry.add("spring.kafka.producer.transaction-id-prefix") { transactionIdPrefix }
        }
    }

    /* Lager topics som brukes i testene */
    @Bean
    fun utbetalingTopic() = NewTopic(utbetalingProperties.utbetalingTopic, 1, 1.toShort())

    @Bean
    fun statusTopic() = NewTopic(utbetalingProperties.statusTopic, 1, 1.toShort())

    @Bean
    fun statistikkTopic() = NewTopic(statistikkProperties.saksBehandlingStatistikkTopic, 1, 1.toShort())

    @Bean
    fun kabalBehandlingEventTopic() = NewTopic(kabalProperties.behandlingEventTopic, 1, 1.toShort())

}
