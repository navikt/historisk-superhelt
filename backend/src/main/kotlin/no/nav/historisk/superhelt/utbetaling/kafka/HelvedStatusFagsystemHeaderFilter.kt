package no.nav.historisk.superhelt.utbetaling.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.adapter.RecordFilterStrategy
import org.springframework.stereotype.Component

@Component
class HelvedStatusFagsystemHeaderFilter: RecordFilterStrategy<String, String> {
    override fun filter(consumerRecord: ConsumerRecord<String?, String?>): Boolean {
        val fagsystemheader = consumerRecord.headers().lastHeader("fagsystem")
        return fagsystemheader== null || "HISTORISK" != String(fagsystemheader.value())
    }
}