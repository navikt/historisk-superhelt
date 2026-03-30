package no.nav.historisk.superhelt.klage.kafka

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SecurityContextUtils
import no.nav.historisk.superhelt.klage.KlageRepository
import no.nav.historisk.superhelt.klage.KlageStatus
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.UUID

/**
 * Konsument for klage-hendingar frå Kabal.
 *
 * Kabal-teamet vil publisere hendingar på topic [KlageKafkaProperties.hendelseTopic]
 * når statusen på ein klage endrar seg (mottatt, ferdigbehandla, feilet o.l.).
 *
 * Meldingsformat er ikkje endeleg avklart med Kabal-teamet – TODO: oppdater
 * [KlageHendelse] og [beregnNyStatus] når format er kjent.
 *
 * Nøkkel (record.key): klage_id (UUID) – same som `kildeReferanse` vi sende til Kabal
 */
@Service
class KlageStatusConsumer(
    properties: KlageKafkaProperties,
    private val klageRepository: KlageRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Starter klage-hendelse consumer for topic: ${properties.hendelseTopic}")
    }

    @KafkaListener(
        topics = ["\${app.klage.hendelse-topic}"],
        groupId = "historisk.superhelt.klage.hendelse",
    )
    fun onHendelse(record: ConsumerRecord<String, String>) {
        val klageId = runCatching { UUID.fromString(record.key()) }.getOrElse {
            logger.warn("Ugyldig klage_id i Kafka-nøkkel: '{}' – hopper over melding", record.key())
            return
        }

        val klage = klageRepository.findById(klageId)
        if (klage == null) {
            logger.warn("Fant ikkje klage med id {} frå Kafka – hopper over", klageId)
            return
        }

        SecurityContextUtils.runAsSystemuser(
            name = "klage-kafka-consumer",
            permissions = listOf(Permission.READ, Permission.WRITE, Permission.IGNORE_TILGANGSMASKIN),
        ) {
            val hendelse = objectMapper.readValue(record.value(), KlageHendelse::class.java)
            val nyStatus = beregnNyStatus(hendelse)
            if (nyStatus != null && nyStatus != klage.status) {
                klageRepository.oppdaterStatus(klageId, nyStatus)
                logger.info("Klage {} oppdatert til status {} (hendelse: {})", klageId, nyStatus, hendelse.type)
            } else {
                logger.debug("Klage {} – ingen statusendring for hendelse {}", klageId, hendelse.type)
            }
        }
    }

    /**
     * TODO: Erstatt med reelle hendingstypar frå Kabal når format er avklart.
     * Placeholder-implementasjon basert på forventa hendingsstruktur.
     */
    private fun beregnNyStatus(hendelse: KlageHendelse): KlageStatus? = when (hendelse.type) {
        KlageHendelseType.MOTTATT    -> KlageStatus.MOTTATT
        KlageHendelseType.FERDIG     -> KlageStatus.FERDIG
        KlageHendelseType.FEILET     -> KlageStatus.FEILET
        KlageHendelseType.UKJENT     -> null.also {
            logger.warn("Ukjent klage-hendingstype '{}' – ignorerer", hendelse.type)
        }
    }
}

/**
 * Placeholder-modell for Kabal-hendings-melding.
 * TODO: oppdater feltar når Kabal-teamet har fastsett meldingsformatet.
 */
data class KlageHendelse(
    /** Hendingstype frå Kabal */
    val type: KlageHendelseType,
    /** Kabal sin interne behandlings-ID (for logging/debug) */
    val kabalBehandlingId: String? = null,
)

/**
 * TODO: avklar med Kabal-teamet kva hendingstypar dei publiserer.
 */
enum class KlageHendelseType {
    MOTTATT,
    FERDIG,
    FEILET,
    UKJENT,
}

