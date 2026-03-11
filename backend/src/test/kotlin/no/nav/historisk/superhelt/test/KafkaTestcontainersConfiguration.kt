package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.statistikk.kafka.StatistikkConfigProperties
import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingConfigProperties
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class KafkaTestcontainersConfiguration(
    private val utbetalingProperties: UtbetalingConfigProperties,
    private val statistikkProperties: StatistikkConfigProperties) {

    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer = INSTANCE

    /* Lager topics som brukes i testene */
    @Bean
    fun utbetalingTopic() = NewTopic(utbetalingProperties.utbetalingTopic, 1, 1.toShort())

    @Bean
    fun statusTopic() = NewTopic(utbetalingProperties.statusTopic, 1, 1.toShort())

    @Bean
    fun statistikkTopic() = NewTopic(statistikkProperties.saksBehandlingStatistikkTopic, 1, 1.toShort())

    companion object {
        val INSTANCE: KafkaContainer =
            KafkaContainer(DockerImageName.parse("apache/kafka-native:4.2.0"))
                .withReuse(true)
                .also { it.start() }
    }
}
