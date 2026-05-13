package no.nav.historisk.mock.kabal

import no.nav.kabal.model.BehandlingEvent
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class MockKabalBehandlingEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val properties: KabalConfigProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendEvent(event: BehandlingEvent) {
        val record = ProducerRecord(properties.behandlingEventTopic, event.kildeReferanse, event as Any)
        logger.info(
            "Sender Kabal BehandlingEvent til topic={} kildeReferanse={} type={}",
            properties.behandlingEventTopic,
            event.kildeReferanse,
            event.type,
        )
        kafkaTemplate.send(record).get()
    }
}
