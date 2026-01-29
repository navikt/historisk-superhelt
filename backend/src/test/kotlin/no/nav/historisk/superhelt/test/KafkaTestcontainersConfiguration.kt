package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.utbetaling.kafka.UtbetalingConfigProperties
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class KafkaTestcontainersConfiguration(private val utbetalingProperties: UtbetalingConfigProperties) {

    @Bean
    @ServiceConnection
    fun kafkaContainer(): ConfluentKafkaContainer {
        return ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.6"))
    }

    /* Lager topics som brukes i testene */
    @Bean
    fun utbetalingTopic() = NewTopic(utbetalingProperties.utbetalingTopic, 1, 1.toShort())

    @Bean
    fun statusTopic() = NewTopic(utbetalingProperties.statusTopic, 1, 1.toShort())

}