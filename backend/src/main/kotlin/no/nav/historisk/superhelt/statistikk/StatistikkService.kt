package no.nav.historisk.superhelt.statistikk

import no.nav.historisk.superhelt.statistikk.kafka.SakStatistikkKafkaProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class StatistikkService(
    private val statistikkKafkaProducer: SakStatistikkKafkaProducer,
    @param:Value("\${info.app.image}") private val appVersion: String) {



}