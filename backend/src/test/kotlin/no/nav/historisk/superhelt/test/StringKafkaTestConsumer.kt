package no.nav.historisk.superhelt.test

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.awaitility.Awaitility.await
import org.springframework.kafka.annotation.KafkaListener
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import java.util.concurrent.TimeUnit

class StringKafkaTestConsumer(val topic: String) {

    private val objectMapper = jacksonObjectMapper()
    val messages = mutableListOf<String>()

    // Unik per instans slik at ulike Spring-kontekster ikke deler consumer group og forstyrrer hverandre
    val groupId = "test-${UUID.randomUUID()}"

    @KafkaListener(
        topics = ["#{__listener.topic}"],
        groupId = "#{__listener.groupId}",
        properties = ["auto.offset.reset=latest"],
    )
    fun receive(record: ConsumerRecord<String, String>) {
        messages.add(record.value())
    }

    val lastMessage: String?
        get() = messages.lastOrNull()

    fun hasMessages(): Boolean = messages.isNotEmpty()

    fun assertMessageReceived(): JsonNode {
        await().atMost(3, TimeUnit.SECONDS).until { hasMessages() }
        val message = lastMessage ?: throw AssertionError("Forventet å finne en melding")
        return objectMapper.readTree(message)
    }


}
