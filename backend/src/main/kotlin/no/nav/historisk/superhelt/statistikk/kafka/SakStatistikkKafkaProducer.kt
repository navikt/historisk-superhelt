package no.nav.historisk.superhelt.statistikk.kafka

import SaksbehandlingsStatistikk
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SakStatistikkKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, SaksbehandlingsStatistikk>,
    properties: StatistikkConfigProperties,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val saksbehandlingStatistikkTopic = properties.saksBehandlingStatistikkTopic

    @Transactional
    fun registrerStatistikk(statistikk: SaksbehandlingsStatistikk) {
        val key = UUID.randomUUID()
        val sakOgBehandling = statistikk.saksnummer + "-" + statistikk.behandlingId
        statistikk.behandlingStatus
        logger.debug(
            "Sender statistikk til topic {} med key {} for sak {} status: {}",
            saksbehandlingStatistikkTopic,
            key,
            sakOgBehandling,
            statistikk.behandlingStatus

        )
        kafkaTemplate.send(saksbehandlingStatistikkTopic, key.toString(), statistikk).get()
    }
}
