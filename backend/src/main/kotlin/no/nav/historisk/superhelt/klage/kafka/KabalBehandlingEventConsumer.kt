package no.nav.historisk.superhelt.klage.kafka

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SecurityContextUtils
import no.nav.historisk.superhelt.klage.config.KabalProperties
import no.nav.kabal.model.BehandlingEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class KabalBehandlingEventConsumer(
    properties: KabalProperties,
    private val klageEventService: KlageEventService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Starter Kabal BehandlingEvent consumer for topic: ${properties.behandlingEventTopic}")
    }

    @KafkaListener(
        topics = ["\${app.kabal.behandling-event-topic}"],
        groupId = "historisk.superhelt.kabal.behandling-events-1",
    )
    fun onBehandlingEvent(record: ConsumerRecord<String, String>) {
        val event = try {
            objectMapper.readValue(record.value(), BehandlingEvent::class.java)
        } catch (e: Exception) {
            logger.error("Klarte ikke deserialisere BehandlingEvent fra Kabal. key={}", record.key(), e)
            return
        }

        if (event.kilde != FORVENTET_KILDE) {
            logger.trace("Ignorerer BehandlingEvent med kilde='{}' (forventet '{}')", event.kilde, FORVENTET_KILDE)
            return
        }

        logger.info(
            "Mottatt BehandlingEvent fra Kabal: eventId={}, type={}, kildeReferanse={}",
            event.eventId, event.type, event.kildeReferanse
        )

        SecurityContextUtils.runAsSystemuser(
            name = "kabal-event-system",
            permissions = listOf(Permission.READ, Permission.WRITE, Permission.IGNORE_TILGANGSMASKIN),
        ) {
            klageEventService.behandleEvent(event)
        }
    }

    companion object {
        private const val FORVENTET_KILDE = "SUPERHELT"
    }
}

