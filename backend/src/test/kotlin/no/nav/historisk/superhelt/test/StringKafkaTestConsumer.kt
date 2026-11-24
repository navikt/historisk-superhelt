package no.nav.historisk.superhelt.test

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.awaitility.Awaitility.await
import org.springframework.kafka.annotation.KafkaListener
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class StringKafkaTestConsumer(val topic: String) {

    private val objectMapper = jacksonObjectMapper()
    private val messages = ConcurrentLinkedQueue<String>()

    // Unik per instans slik at ulike Spring-kontekster ikke deler consumer group og forstyrrer hverandre
    val groupId = "test-${UUID.randomUUID()}"

    @KafkaListener(
        topics = ["#{__listener.topic}"],
        groupId = "#{__listener.groupId}",
        properties = ["auto.offset.reset=earliest"],
    )
    fun receive(record: ConsumerRecord<String, String>) {
        messages.add(record.value())
    }

    fun assertMessageReceived(predicate: (JsonNode) -> Boolean): JsonNode {
        await().atMost(3, TimeUnit.SECONDS).until {
            messages.any { predicate(objectMapper.readTree(it)) }
        }

        return messages.firstNotNullOfOrNull { message ->
            objectMapper.readTree(message).takeIf(predicate)
        } ?: throw AssertionError("Forventet å finne en melding som samsvarer med vilkåret")
    }
}
