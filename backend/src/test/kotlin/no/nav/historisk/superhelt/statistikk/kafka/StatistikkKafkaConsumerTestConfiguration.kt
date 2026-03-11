package no.nav.historisk.superhelt.statistikk.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener

@TestConfiguration(proxyBeanMethods = false)
class StatistikkKafkaConsumerTestConfiguration(private val statistikkProperties: StatistikkConfigProperties) {

    @Bean
    fun sakStatistikkKafkaConsumer() = StringKafkaConsumer(topic = statistikkProperties.saksBehandlingStatistikkTopic)
}

class StringKafkaConsumer(val topic: String) {

    val messages = mutableListOf<String>()

    @KafkaListener(
        topics = ["#{__listener.topic}"],
        groupId = "historisk.superhelt.test",
    )
    fun statusMessage(record: ConsumerRecord<String, String>) {
        messages.add(record.value())
    }

    val lastMessage: String?
        get() = messages.lastOrNull()
    fun hasMessages(): Boolean = messages.isNotEmpty()
}

